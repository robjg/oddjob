package org.oddjob.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.spi.ToolProvider;

/**
 * Main to run the Reference doclet with default options. Note this has been moved here from the
 * oj-docs module because it was failing to descriptors for module on the doclet classpath because
 * oj-docs was required on the application classpath for this Main, and thus the doclet was
 * being loaded on the application classpath, not the doclet classpath. Hence, descriptors couldn't be found.
 *
 */
public class ReferenceMain implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceMain.class);

    public static final String SOURCE_PATH_OPTION = "-sourcepath";

    public static final String DESTINATION_OPTION = "-d";

    public static final String DESCRIPTOR_URL_OPTION = "-descriptorurl";

    public static final String TITLE_OPTION = "-t";

    public static final String LOADER_PATH_OPTION = "-loaderpath";

    public static final String WRITER_FACTORY_OPTION = "-writerfactory";

    public static final String API_URL_OPTION = "-link";

    public static final String REFERENCE_DOCLET_CLASS_NAME = "org.oddjob.doc.doclet.ReferenceDoclet";

    private String name;

    private String sourcepath;

    private String directory;

    private String packages;

    private String classPath;

    private String docletPath;

    private String loaderPath;

    private List<String> descriptorUrls;

    private String writerFactory;

    private List<String> links;

    private boolean verbose;

    public static void main(String... args) {

        int result = mainCall(args);
        System.exit(result);
    }

    public static int mainCall(String... args) {

        ReferenceMain main = new ReferenceMain();

        List<String> links = new ArrayList<>();
        List<String> descriptorUrls = new ArrayList<>();

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (SOURCE_PATH_OPTION.equals(arg)) {
                main.setSourcepath(args[++i]);
                continue;
            }
            if (DESTINATION_OPTION.equals(arg)) {
                main.setDirectory(args[++i]);
                continue;
            }
            if (LOADER_PATH_OPTION.equals(arg)) {
                main.setLoaderPath(args[++i]);
                continue;
            }
            if (DESCRIPTOR_URL_OPTION.equals(arg)) {
                descriptorUrls.add(args[++i]);
                continue;
            }
            if (API_URL_OPTION.equals(arg)) {
                links.add(args[++i]);
                continue;
            }
            if (i == args.length - 1) {
                main.setPackages(args[i]);
            }
        }

        main.setDescriptorUrls(descriptorUrls);
        main.setLinks(links);

        return main.call();
    }

    @Override
    public Integer call() {

        String sourcepath = Objects.requireNonNullElse(this.sourcepath, "src/main/java");

        String dest = Objects.requireNonNullElse(this.directory, "docs/reference");

        String packages = Objects.requireNonNullElse(this.packages, "org.oddjob");

        ToolProvider toolProvider = ToolProvider.findFirst("javadoc")
                .orElseThrow(() -> new IllegalArgumentException("No JavaDco"));

        List<String> args = new ArrayList<>();
        Optional.ofNullable(this.classPath).ifPresent(dp -> {
            args.add("-classpath");
            args.add(classPath);
        });
        args.add("-doclet");
        args.add(REFERENCE_DOCLET_CLASS_NAME);
        Optional.ofNullable(this.docletPath).ifPresent(dp -> {
            args.add("-docletpath");
            args.add(dp);
        });
        args.add(SOURCE_PATH_OPTION);
        args.add(sourcepath);
        args.add("--ignore-source-errors");
        args.add(DESTINATION_OPTION);
        args.add(dest);
        args.add("-private");
        args.add("-subpackages");
        args.add(packages);
        if (this.verbose) {
            args.add("-verbose");
        }
        Optional.ofNullable(this.loaderPath).ifPresent(lp -> {
            args.add(LOADER_PATH_OPTION);
            args.add(lp);
        });
        Optional.ofNullable(this.descriptorUrls).ifPresent(ds -> {
            ds.forEach(url -> {
                args.add(DESCRIPTOR_URL_OPTION);
                args.add(url);
            });
        });
        Optional.ofNullable(this.writerFactory).ifPresent(wf -> {
            args.add(WRITER_FACTORY_OPTION);
            args.add(wf);
        });
        Optional.ofNullable(this.links).ifPresent(urls -> {
            urls.forEach(url ->  {
                args.add(API_URL_OPTION);
                args.add(url);
            });
        });

        logger.info("Running javadoc with {}", args);

        return toolProvider.run(System.out, System.err, args.toArray(new String[0]));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourcepath() {
        return sourcepath;
    }

    public void setSourcepath(String sourcepath) {
        this.sourcepath = sourcepath;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getDocletPath() {
        return docletPath;
    }

    public void setDocletPath(String docletPath) {
        this.docletPath = docletPath;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getLoaderPath() {
        return loaderPath;
    }

    public void setLoaderPath(String loaderPath) {
        this.loaderPath = loaderPath;
    }

    public String getWriterFactory() {
        return writerFactory;
    }

    public List<String> getDescriptorUrls() {
        return descriptorUrls;
    }

    public void setDescriptorUrls(List<String> descriptorUrls) {
        this.descriptorUrls = descriptorUrls;
    }

    public void setWriterFactory(String writerFactory) {
        this.writerFactory = writerFactory;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String toString() {
        return Objects.requireNonNullElseGet(this.name, () -> getClass().getSimpleName());
    }
}
