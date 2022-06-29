package org.oddjob.jobs;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.beanbus.SimpleBusConductor;
import org.oddjob.beanbus.destinations.BeanDiagnostics;
import org.oddjob.beanbus.drivers.IterableBusDriver;
import org.oddjob.io.StdoutType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author rob
 * @oddjob.description Dump out the types and properties of a bean or
 * beans.
 */
public class BeanDiagnosticsJob implements Runnable, ArooaSessionAware {

//	private static final Logger logger = LoggerFactory.getLogger(BeanReportJob.class);

    /**
     * @oddjob.property
     * @oddjob.description The name of this job.
     * @oddjob.required No.
     */
    private String name;

    /**
     * @oddjob.property
     * @oddjob.description A single bean to analyse.
     * @oddjob.required Either this or the beans are required.
     */
    private Object bean;

    /**
     * @oddjob.property
     * @oddjob.description The beans to analyse.
     * @oddjob.required Either this or a bean is required.
     */
    private Iterable<?> beans;

    /**
     * @oddjob.property
     * @oddjob.description The Output to where the report will
     * be written.
     * @oddjob.required Yes.
     */
    private OutputStream output;


    /**
     * Required for bean access.
     */
    private ArooaSession session;


    @ArooaHidden
    @Override
    public void setArooaSession(ArooaSession session) {
        this.session = session;
    }

    @Override
    public void run() {

        Iterable<?> beans = this.beans;

        if (beans == null) {
            beans = Collections.singletonList(Objects.requireNonNull(this.bean, "No Beans"));
        }

        try (OutputStream out = Optional.ofNullable(this.output)
                .orElseGet(() ->  new StdoutType().toOutputStream())) {

            IterableBusDriver<Object> iterableBusDriver =
                    new IterableBusDriver<>();

            BeanDiagnostics<Object> diagnostics = new BeanDiagnostics<>();
            diagnostics.setArooaSession(session);
            diagnostics.setOutput(output);

            iterableBusDriver.setValues(beans);
            iterableBusDriver.setTo(diagnostics);

            SimpleBusConductor busConductor = new SimpleBusConductor(iterableBusDriver, diagnostics);
            busConductor.run();
            busConductor.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Iterable<?> getBeans() {
        return beans;
    }

    public void setBeans(Iterable<?> beans) {
        this.beans = beans;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    @Override
    public String toString() {
        if (name == null) {
            return getClass().getSimpleName();
        }
        return name;
    }

}
