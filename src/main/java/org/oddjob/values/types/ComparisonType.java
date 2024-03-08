package org.oddjob.values.types;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @oddjob.description Provides a Predicate from simple checks.
 *
 * @author rob
 */
public class ComparisonType<T>
        implements ValueFactory<Predicate<T>>, ArooaSessionAware {
    private static final Logger logger = LoggerFactory.getLogger(ComparisonType.class);

    /**
     * @oddjob.property null
     * @oddjob.description Must the value be null for the check to pass.
     * True the value must be null. False it must not be null. If this
     * property is true other checks will cause an exception because they
     * require the value property has a value.
     * @oddjob.required No, if this does exist the check value null will fail.
     */
    private boolean null_;

    /**
     * @oddjob.property
     * @oddjob.description The value to check.
     * @oddjob.required No, but the check value is not null will fail.
     */
    private Boolean z;

    /**
     * @oddjob.property
     * @oddjob.description The value must be equal to this.
     * @oddjob.required No.
     */
    private ArooaValue eq;

    /**
     * @oddjob.property
     * @oddjob.description The value must be not equal to this.
     * @oddjob.required No.
     */
    private ArooaValue ne;

    /**
     * @oddjob.property
     * @oddjob.description The value must be less than this.
     * @oddjob.required No.
     */
    private ArooaValue lt;

    /**
     * @oddjob.property
     * @oddjob.description The value must be less than or equals to this.
     * @oddjob.required No.
     */
    private ArooaValue le;

    /**
     * @oddjob.property
     * @oddjob.description The value must be greater than this.
     * @oddjob.required No.
     */
    private ArooaValue gt;

    /**
     * @oddjob.property
     * @oddjob.description The value must be greater than or equal to this.
     * @oddjob.required No.
     */
    private ArooaValue ge;

    private ArooaConverter converter;

    /**
     * @oddjob.property
     * @oddjob.description The name of this job. Can be any text.
     * @oddjob.required No.
     */
    private transient String name;

    private transient final List<Check<T>> checks = List.of(

            new Check<>() {
                @Override
                public boolean required() {
                    return true;
                }

                @Override
                public boolean check(T value) {
                    return (value == null) == null_;
                }

                @Override
                public String toString() {
                    return "should" +
                            (null_ ? "" : " not") + " be null";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return z != null;
                }

                @Override
                public boolean check(T value) {
                    return value != null &&
                            (value.toString().isEmpty())
                                    == z;
                }

                @Override
                public String toString() {
                    return "should be of " +
                            (z ? "" : "none") +
                            " zero length";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return eq != null;
                }

                @Override
                public boolean check(T value) {
                    return value != null && value.equals(convert(value, eq));
                }

                @Override
                public String toString() {
                    return "should equal [" + eq + "]";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return ne != null;
                }

                @Override
                public boolean check(T value) {
                    return value != null && !value.equals(convert(value, ne));
                }

                @Override
                public String toString() {
                    return "should not equal [" + ne + "]";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return lt != null;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public boolean check(T value) {
                    //noinspection unchecked
                    return value != null &&
                            ((Comparable) value).compareTo(
                                    convert(value, lt)) < 0;
                }

                @Override
                public String toString() {
                    return "should be less than [" +
                            lt + "]";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return le != null;
                }

                @Override
                @SuppressWarnings("rawtypes")
                public boolean check(T value) {
                    //noinspection unchecked
                    return value != null &&
                            ((Comparable) value).compareTo(
                                    convert(value, le)) <= 0;
                }

                @Override
                public String toString() {
                    return "should be less or equal to [" + le + "]";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return gt != null;
                }

                @Override
                @SuppressWarnings("rawtypes")
                public boolean check(T value) {
                    //noinspection unchecked
                    return value != null &&
                            ((Comparable) value).compareTo(
                                    convert(value, gt)) > 0;
                }

                @Override
                public String toString() {
                    return "should be greater than [" +
                            gt + "]";
                }
            },
            new Check<>() {
                @Override
                public boolean required() {
                    return ge != null;
                }

                @Override
                @SuppressWarnings("rawtypes")
                public boolean check(T value) {
                    //noinspection unchecked
                    return value != null &&
                            ((Comparable) value).compareTo(
                                    convert(value, ge)) >= 0;
                }

                @Override
                public String toString() {
                    return "should be greater or equal to [" +
                            ge + "]";
                }
            }
    );


    @Override
    public Predicate<T> toValue() {

        StringBuilder toStrings = new StringBuilder();

        List<Check<T>> checks = this.checks
                .stream()
                .filter(Check::required)
                .peek(p -> {
                    if (toStrings.length() > 0) {
                        toStrings.append(" and ");
                    }
                    toStrings.append(p);
                })
                .collect(Collectors.toList());

        String toString = toStrings.toString();

        logger.info("Check(s) passed.");

        return new Predicate<>() {
            @Override
            public boolean test(T t) {
                for (Check<T> check : checks) {
                    if (!check.check(t)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public String toString() {
                return toString;
            }
        };
    }

    /**
     * Encapsulate a check.
     */
    interface Check<T> {
        /**
         * Is a check required?
         *
         * @return true if it is, false if it isn't required.
         */
        boolean required();

        /**
         * Perform the check.
         *
         * @return true if it passed, false if it didn't.
         */
        boolean check(T value);
    }

    /**
     * Convert the Right Hand Side of the expression to the type of the
     * value property.
     *
     * @param value The value being tested against.
     * @param rhs Right Hand Side
     *
     * @return The converted value.
     */
    Object convert(Object value, ArooaValue rhs) {

        try {
            return converter.convert(rhs, value.getClass());
        } catch (NoConversionAvailableException | ConversionFailedException e) {
            throw new RuntimeException(e);
        }
    }

    @ArooaHidden
    public void setArooaSession(ArooaSession session) {
        this.converter = session.getTools().getArooaConverter();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getNull() {
        return null_;
    }

    @ArooaAttribute
    public void setNull(boolean value) {
        this.null_ = value;
    }

    public Boolean getZ() {
        return z;
    }

    public void setZ(Boolean z) {
        this.z = z;
    }

    public ArooaValue getEq() {
        return eq;
    }

    @ArooaAttribute
    public void setEq(ArooaValue eq) {
        this.eq = eq;
    }

    public ArooaValue getNe() {
        return ne;
    }

    @ArooaAttribute
    public void setNe(ArooaValue ne) {
        this.ne = ne;
    }

    public ArooaValue getLt() {
        return lt;
    }

    @ArooaAttribute
    public void setLt(ArooaValue lt) {
        this.lt = lt;
    }

    public ArooaValue getGt() {
        return gt;
    }

    @ArooaAttribute
    public void setGt(ArooaValue gt) {
        this.gt = gt;
    }

    public ArooaValue getLe() {
        return le;
    }


    @ArooaAttribute
    public void setLe(ArooaValue le) {
        this.le = le;
    }


    public ArooaValue getGe() {
        return ge;
    }


    @ArooaAttribute
    public void setGe(ArooaValue ge) {
        this.ge = ge;
    }

    @Override
    public String toString() {
        return name == null ? ComparisonType.class.getSimpleName() : name;
    }
}
