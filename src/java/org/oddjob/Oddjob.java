package org.oddjob;


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.deploy.ArooaDescriptorBean;
import org.oddjob.arooa.deploy.ArooaDescriptorFactory;
import org.oddjob.arooa.deploy.ListDescriptorBean;
import org.oddjob.arooa.deploy.NoAnnotations;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.ConfigConfigurationSession;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationOwnerSupport;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.HandleConfigurationSession;
import org.oddjob.arooa.parsing.OwnerStateListener;
import org.oddjob.arooa.parsing.SerializableDesignFactory;
import org.oddjob.arooa.parsing.SessionStateListener;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.registry.ServiceProvider;
import org.oddjob.arooa.registry.Services;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.utils.RootConfigurationFileCreator;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.designer.components.RootDC;
import org.oddjob.framework.ComponentBoundry;
import org.oddjob.framework.StructuralJob;
import org.oddjob.input.InputHandler;
import org.oddjob.jobs.EchoJob;
import org.oddjob.oddballs.OddballsDescriptorFactory;
import org.oddjob.persist.FilePersister;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.OddjobServicesBean;
import org.oddjob.sql.SQLPersisterService;
import org.oddjob.state.IsHardResetable;
import org.oddjob.state.IsNot;
import org.oddjob.state.IsSoftResetable;
import org.oddjob.state.ParentState;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;
import org.oddjob.util.OddjobConfigException;
import org.oddjob.util.URLClassLoaderType;
import org.oddjob.values.properties.PropertiesType;

/**
 * Read a configuration, creates child jobs and executes them. 
 *
 * @oddjob.description The starting point for a hierarchy of jobs. The Oddjob job
 * creates and runs a job hierarchy by processing a supplied configuration.
 * <p>
 * Oddjob creates a 'root' job on which to create the hierarchy. Through this 
 * root Oddjob aquires the first job to run and also exposes some of it's own
 * properties for the jobs in the configuration to use. The root job's properties 
 * are:
 * 
 * <dl>
 *  <dt><b>job</b></dt>
 *  <dd>The top level job. This is the single job that Oddjob runs. This property is 
 *      optional but Oddjob won't do much if a job for it to run isn't supplied. 
 *      This is the Oddjob root's only writeable property and is write only.</dd>
 *  <dt><b>file</b></dt>
 *  <dd>The path of the configuration file that Oddjob has loaded. Read Only.</dd>
 *  <dt><b>dir</b></dt>
 *  <dd>The path of the configuration file's directory. Read Only.</dd>
 *  <dt><b>args</b></dt>
 *  <dd>An array of arguments passed in on the command line or from a parent 
 *      Oddjob. See below. Read Only.</dd>
 *  <dt><b>services</b></dt>
 *  <dd>Provides access to Oddjobs underlying services. Used by
 *      the frameworks automatic configuration mechanism to configure the properties
 *      of jobs that are documented as set automatically. May be ignored for every day
 *      use. Read Only.</dd>
 * </dl>
 * 
 * <p>For these properties to be accessible the root oddjob must be given an id. 
 * As can be seen from the examples, the author uses the id '<code>this</code>'
 * but the choice is arbitrary.</p>
 * 
 * <h4>Nesting Oddjobs</h4>
 * 
 * <p>
 * An Oddjob job allows an Oddjob instance to be created within an existing Oddjob
 * configuration. This way complicated processes can be created in manageable and
 * separately testable units.  
 * <p>
 * Properties of jobs in a nested Oddjob can be accessed using the notation 
 * <i>${nested-oddjob-id/job-id.property}</i> where nested-oddjob-id is the id in
 * the outer configuration, not the inner one.
 * 
 * <h4>Saving Oddjob's State</h4>
 * 
 * <p>
 * The <code>persister</code> property on a nested Oddjob will allow it's state to
 * be saved. See the 
 * <a href="http://rgordon.co.uk/projects/oddjob/userguide/saving.html">User Guide</a>
 * for more information on how to set a persister.
 * 
 * <h4>Customising Oddjob</h4>
 * 
 * <p>
 * Oddjob's <code>descriptorFactory</code> and <code>classLoader</code> properties 
 * allow bespoke components and
 * types to be used. The
 * <a href="http://rgordon.co.uk/projects/oddjob/devguide/index.html">developer guide</a>
 *  is all about writing custom job's for Oddjob. 
 * <p>
 * 
 * 
 * @oddjob.example
 * 
 * Hello World with Oddjob. Oddjob is configured to run the {@link EchoJob} job.
 * 
 * {@oddjob.xml.resource org/oddjob/HelloWorld.xml}
 * 
 * @oddjob.example
 * 
 * Using an argument passed into Oddjob that may or may not be set.
 * 
 * {@oddjob.xml.resource org/oddjob/OptionalFileNameArg.xml}
 * 
 * @oddjob.example
 * 
 * Nesting Oddjob. Note how the <code>dir</code> property of the
 * Oddjob root is used as the path of the nested configuration file.
 * 
 * {@oddjob.xml.resource org/oddjob/NestedOddjob.xml}
 * 
 * <p>
 * The nested job is the first example:
 * 
 * {@oddjob.xml.resource org/oddjob/HelloWorld.xml}
 * 
 * This example also shows how a property within the nested file can be
 * accessed within the parent configuration. 
 * 
 * @oddjob.example
 * 
 * A nested Oddjob with one argument passed to the child.
 * 
 * {@oddjob.xml.resource org/oddjob/NestedOddjobWithArg.xml}
 * 
 * And EchoArg.xml:
 * 
 * {@oddjob.xml.resource org/oddjob/EchoArg.xml}
 * 
 * @oddjob.example
 * 
 * A nested Oddjob with a property past to the child.
 * 
 * {@oddjob.xml.resource org/oddjob/NestedOddjobWithProperty.xml}
 * 
 * And EchoProperty.xml:
 * 
 * {@oddjob.xml.resource org/oddjob/EchoProperty.xml}
 * 
 * Unlike the properties of jobs, free format properties like this can't be
 * accessed using the nested convention.
 * <pre>
 * ${nested/our.greeting} DOES NOT WORK!
 * </pre>
 * This may be fixed in future versions.
 * 
 * @oddjob.example
 * 
 * Using export to pass values to a nested Oddjob.
 * 
 * {@oddjob.xml.resource org/oddjob/OddjobExportJobTest.xml}
 * 
 * Here a job is exported into a nested Oddjob. The
 * exported object is actually a {@link ValueType}. The value is converted back
 * to the job when the job property of the run job is set. Expressions such
 * as <code>${secret.text}</code> are not valid (because value does not have a 
 * text property!). Even <code>${secret.value.text}</code> will not work because
 * of value wraps the job in yet another layer of complexity.
 * 
 * @oddjob.example
 * 
 * Examples elsewhere.
 * <ul>
 *  <li>{@link FilePersister}</li>
 *  <li>{@link SQLPersisterService}</li>
 *  <li>{@link OddballsDescriptorFactory}</li>
 *  <li>{@link URLClassLoaderType}</li>
 * </ul>
 * <p>
 * 
 * @author Rob Gordon
 * 
 */
public class Oddjob extends StructuralJob<Object>
implements Loadable, 
		ConfigurationOwner,
		BeanDirectoryOwner {
	
    private static final long serialVersionUID = 2010051200L;
	
    /** The document root for an Oddjob configuration file. */
    public static final ArooaElement ODDJOB_ELEMENT = 
    	new ArooaElement("oddjob");
    
    /** For console capture. */
    private volatile transient OddjobConsole.Close console;
    
	/** The configuration file. */
	private volatile File file;
		
	/** This will be true when there is a restored configuration.
	 * Setting a new configuration will be ignored while this flag
	 * is true. A reset operation will clear this flag. */
	private transient volatile boolean restored;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The configuration. An alternative to
	 * setting a file. This can be useful when the configuration
	 * is to come from some other input.
	 * <p>
	 * See also {@link XMLConfigurationType}
	 * 
	 * @oddjb.required Either this or file is required.
	 */
	private transient volatile ArooaConfiguration configuration;
	
	/** Support for configuration modification. */
	private transient volatile ConfigurationOwnerSupport configurationOwnerSupport;
	
	/** 
	 * @oddjob.property 
	 * @oddjob.description A component which is able to save and restore
	 * jobs.
	 * <p>
	 * See also {@link FilePersister} and {@link SQLPersisterService}.
	 * 
	 * @oddjob.required No.
	 */
	private transient volatile OddjobPersister persister;
		
	/** 
	 * @oddjob.property
	 * @oddjob.description An array of arguments the Oddjob configuration can use.
	 * @oddjob.required No.
	 */
	private volatile String[] args;
	
	/** Class loader */
	private transient volatile ClassLoader classLoader;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description An {@link ArooaDescriptorFactory} that
	 * will be used when loading the configuration. This augments Oddjob's
	 * internal descriptor, and allows custom jobs to have their own
	 * definitions.
	 * <p>
	 * See also {@link ArooaDescriptorBean}, {@link ListDescriptorBean}
	 * and {@link OddballsDescriptorFactory} 
	 * 
	 * @oddjob.required No.
	 */
	private transient volatile ArooaDescriptorFactory descriptorFactory;
	
	/** The session that is created on loading. */
	private transient ArooaSession ourSession;
	
	/** The root. */
	private transient Object oddjobRoot;
	
	/** Executors used if none are provided. */
	private transient volatile DefaultExecutors internalExecutors;
	
	/** 
	 * @oddjob.property 
	 * @oddjob.description Executors for Oddjob to use. This is
	 * set automatically in Oddjob. For advanced use, user 
	 * supplied {@link OddjobExecutors} may be provided.
	 * @oddjob.required No.
	 */
	private transient volatile OddjobExecutors oddjobExecutors;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Services for Oddjob to use. This is
	 * set automatically in Oddjob. Unlikely to be required.
	 * @oddjob.required No.
	 */
	private transient volatile OddjobServices oddjobServices;
	
	/**
	 * @oddjob.property
	 * @oddjob.description Values to be exported into the nested 
	 * configuration. Values will be registered in the inner
	 * oddjob using the key of this mapped property.
	 * @oddjob.required No
	 */
	private transient volatile Map<String, ArooaValue> export;

	/**
	 * @oddjob.property
	 * @oddjob.description Properties to be set in the nested 
	 * configuration. Can be set using a {@link PropertiesType}.
	 * @oddjob.required No
	 */
	private volatile Properties properties;
	
	/**
	 * @oddjob.property
	 * @oddjob.description Set how an Oddjob should share the values and
	 * properties of it's parent. Valid values are:
	 * <dl>
	 * <dt>NONE</dt>
	 * <dd>No values or properties are automatically inherited.</dd>
	 * <dt>PROPERTIES</dt>
	 * <dd>All properties are inherited. Only properties are inherited, values
	 * must be exported explicitly using the export property.</dd>
	 * <dt>SHARED</dt>
	 * <dd>All properties and values are shared between the parent and child
	 * Oddjobs. Any properties or values set in the child will be visible
	 * in the parent. This setting is particularly useful for shared common
	 * configuration.</dd>
	 * @oddjob.required No. Defaults to PROPERTIES.
	 */
	private volatile OddjobInheritance inheritance;
	
	/**
	 * Values for resets.
	 */
	enum Reset {
		SOFT,
		HARD;
	}

	/**
	 * @oddjob.property
	 * @oddjob.description Used internally to remember which
	 * reset to apply after loading a configuration.
	 */
	private Reset lastReset;
	
	/**
	 * @oddjob.property
	 * @oddjob.description A handler for user input. This will be 
	 * provided internally and will only be required in specialised
	 * situations.
	 * @oddjob.required No.
	 */
	private transient InputHandler inputHandler;
	
	/**
	 * Only constructor.
	 */
	public Oddjob() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		console = OddjobConsole.initialise();
		configurationOwnerSupport =
			new ConfigurationOwnerSupport(this);		
	}
	
	/**
	 * @oddjob.property file
	 * @oddjob.description The name of the configuration file.
	 * to configure this oddjob.
	 * @oddjob.required No.
	 * 
	 * @return The file name.
	 */
	@ArooaAttribute
	public void setFile(File file) {
		if (restored) {
			return;
		}

		this.file = file;
		if (file == null) {
			// during destroy clean up. Does this matter?
			configuration = null;
		}
	}

	public File getFile() {
		if (file == null) {
			return null;
		}
		return file.getAbsoluteFile();
	}
	
	/**
	 * Setter for configuration.
	 * 
	 * @param config
	 */
	public void setConfiguration(ArooaConfiguration config) {
		if (restored) {
			return;
		}
		this.configuration = config;
	}
		
	/** 
	 * @oddjob.property classLoader 
	 * @oddjob.description The classLoader to use when loading
	 * the configuration.
	 * <p>
	 * See also {@link URLClassLoaderType}
	 * 
	 * @oddjob.required No.
	 */
	public void setClassLoader(ClassLoader classLoader) {
	    this.classLoader = classLoader;
	}
	
	/**
	 * Return a class loader. If one has not been set return
	 * the class loader which loaded this class.
	 * 
	 * @return A classLoader.
	 */
	public ClassLoader getClassLoader() {
	    return this.classLoader;
	}	
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.parsing.ConfigurationOwner#provideConfigurationSession()
	 */
	@Override
	public ConfigurationSession provideConfigurationSession() {
		
		return configurationOwnerSupport.provideConfigurationSession();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.parsing.ConfigurationOwner#addOwnerStateListener(org.oddjob.arooa.parsing.OwnerStateListener)
	 */
	@Override
	public void addOwnerStateListener(OwnerStateListener listener) {
		configurationOwnerSupport.addOwnerStateListener(listener);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.parsing.ConfigurationOwner#removeOwnerStateListener(org.oddjob.arooa.parsing.OwnerStateListener)
	 */
	@Override
	public void removeOwnerStateListener(OwnerStateListener listener) {
		configurationOwnerSupport.removeOwnerStateListener(listener);
	}
	
	@Override
	public SerializableDesignFactory rootDesignFactory() {
		return new RootDC();
	}
	
	@Override
	public ArooaElement rootElement() {
		return ODDJOB_ELEMENT;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.framework.StructuralJob#getStateOp()
	 */
	@Override
	protected StateOperator getInitialStateOp() {
		return new WorstStateOp();
	}

	/**
	 * Set our internal session. Can't remember why this is a method.
	 * @param session
	 */
	private void setOurSession(ArooaSession session) {
		this.ourSession = session;
	}

	/**
	 * Shared initialisation between load and execute.
	 * 
	 * @return
	 */
	private OddjobServices preLoadInitialisation() {
		
		// When configured by a file, check it exists and use
		// it as Oddjob's configuration.
		if (file != null) {
			new RootConfigurationFileCreator(
					ODDJOB_ELEMENT).createIfNone(file);
			configuration = new XMLConfiguration(file);
		}		
		
		if (configuration == null){
			throw new IllegalStateException("No configuration given.");
		}
       
		ClassLoader classLoader = this.classLoader;
		if (classLoader == null) {
			if (oddjobServices == null) {
				classLoader = getClass().getClassLoader();
			}
			else {
				classLoader = oddjobServices.getClassLoader();
			}
		}
		OddjobExecutors oddjobExecutors = this.oddjobExecutors;
		if (oddjobExecutors == null) {
			if (oddjobServices == null) {
				internalExecutors = new DefaultExecutors();			    	    
				oddjobExecutors = internalExecutors;
			}
			else {
				oddjobExecutors = oddjobServices.getOddjobExecutors();
			}
		}
		InputHandler inputHandler = this.inputHandler;
		if (inputHandler == null && oddjobServices != null) {
				inputHandler = oddjobServices.getInputHandler();
		}
		
		OddjobServicesBean services = new OddjobServicesBean();
		services.setClassLoader(classLoader);
		services.setOddjobExecutors(oddjobExecutors);
		services.setInputHandler(inputHandler);
		
		OddjobSessionFactory sessionFactory = new OddjobSessionFactory();
		sessionFactory.setExistingSession(getArooaSession());
		sessionFactory.setClassLoader(classLoader);
		sessionFactory.setDescriptorFactory(descriptorFactory);
		sessionFactory.setOddjobPersister(persister);
		sessionFactory.setProperties(properties);
		sessionFactory.setInherit(inheritance);
		
		ArooaSession newSession = sessionFactory.createSession(this);
        
        if (export != null) {
    		ArooaConverter converter = 
    				newSession.getTools().getArooaConverter();
    		
        	for (Map.Entry<String, ArooaValue> entry: export.entrySet()) {
        		String name = entry.getKey();
        		ArooaValue value = entry.getValue();
        		
            	try {
            		ArooaObject object = 
            				converter.convert(value, ArooaObject.class);
            		
            		if (object == null) {
            			logger().info("Bean to export with id " + 
            					name + " is null.");
            		}
            		else {
            			newSession.getBeanRegistry().register(
            					name, object.getValue());
            		}
            	}
            	catch (ArooaConversionException e) {
        			newSession.getBeanRegistry().register(
        					name, value);
            	}        		
        	}
        }

        // Bean directory needs to be available during creation.
        setOurSession(newSession);
          		
		return services;
	}
	
	
	/**
	 * Load Oddjob from the configuration file.
	 */
	private void doLoad(OddjobServices oddjobServices) throws Exception {
        logger().info("Loading from configuration " +
        		configuration);

		
        oddjobRoot = new OddjobRoot(oddjobServices);
        	        	        
        StandardArooaParser parser = new StandardArooaParser(
        		oddjobRoot, ourSession);

        parser.setExpectedDocumentElement(
        		ODDJOB_ELEMENT);

        try {
	        ConfigurationHandle configHandle = parser.parse(configuration);
	        
	        configurationOwnerSupport.setConfigurationSession(
	        		new OddjobConfigurationSession(
	        				new HandleConfigurationSession(
	        						ourSession, configHandle)));
        }
        catch (Exception e) {
            // This will edit a failed configuration in case of
            // failure.
            configurationOwnerSupport.setConfigurationSession(
            				new ConfigConfigurationSession(
            						ourSession, configuration));			
	        // Reset on failure.
        	setOurSession(null);
        	throw e;
        }
        logger().debug("Loaded.");
	}

	/**
	 * @oddjob.property loadable
	 * @oddjob.description Can Oddjob be loaded. Used by the Load/Unload actions.
	 * @oddjob.required Read only.
	 */
	@Override
	public boolean isLoadable() {
		return ourSession == null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.Loadable#load()
	 */
	@Override
	public void load() {
		ComponentBoundry.push(loggerName(), this);
		try {
			stateHandler().waitToWhen(new IsNot(StateConditions.RUNNING), 
					new Runnable() {
				public void run() {
				    try {
						if (ourSession != null) {
							return;
						}
	
				    	configure();
				    	
						OddjobServices services = preLoadInitialisation();
						
						doLoad(services);
				    } 
				    catch (Exception e) {
						logger().error("Exception executing job.", e);
						getStateChanger().setStateException(e);
				    }
				}
			});
		}
		finally {
			ComponentBoundry.pop();
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.jobs.AbstractJob#execute()
	 */
	@Override
	protected void execute() throws Exception {
	    
		if (ourSession == null) {
			OddjobServices services = preLoadInitialisation();

			doLoad(services);			
			
			if (lastReset != null) {
				switch (lastReset) {
				case SOFT:
					logger().debug("Re-doing Soft Reset on children after load.");
					childHelper.softResetChildren();
					break;
				case HARD:
					logger().debug("Re-doing Hard Reset on children after load.");
					childHelper.hardResetChildren();
					break;
				}		
				lastReset = null;
			}
		}

		Object child = childHelper.getChild();

		// if there is something to execute, execute it.
		if (child != null && child instanceof Runnable && !stop) {
			((Runnable) child).run();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.Loadable#unload()
	 */
	@Override
	public void unload() {
		reset();
	}
	
	/**
	 * Actions to be performed when resetting Oddjob.
	 *
	 */
	private void reset() {
		
		if (ourSession == null) {
			// being destroyed but hasn't been loaded.
			return;
		}
		
		configurationOwnerSupport.setConfigurationSession(null);
		
		ComponentPersister persister = ourSession.getComponentPersister();		
		if (persister != null) {
			persister.close();
		}
		
		ArooaContext oddjobContext = 
			ourSession.getComponentPool().contextFor(oddjobRoot);

		// Context could be null if the document element was rubbish.
		if (oddjobContext != null) {
			oddjobContext.getRuntime().destroy();
		}

		oddjobRoot = null;
		setOurSession(null);
	}

	/**
	 * Part of Oddjob's internals. Stop the Executors, if we started them.
	 */
	void stopExecutors() {
		
		// Try and be thread safe about the null pointer possibility.
		DefaultExecutors executors = internalExecutors;
		if (executors != null) {
			executors.stop();
			internalExecutors = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
				
		reset();
		
		stopExecutors();
		console.close();
	}
	

	@Override
	public boolean softReset() {
		
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler().waitToWhen(new IsSoftResetable(), new Runnable() {
				public void run() {

					logger().debug("Soft reset requested.");
					if (ourSession == null) {					
						if (lastReset == null) {
							lastReset = Reset.SOFT;
							if (!saveLastReset()) {
								return;
							}
						}
					}
					else {
						lastReset = null;
					}

					superSoftReset();
					restored = false;
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}

	/**
	 * Required because Java doesn't support Oddjob.this.super.softReset from 
	 * within inner classes.
	 */
	private void superSoftReset() {
		super.softReset();
	}
	
	/**
	 * Save the last reset level. Required because there may be a break
	 * between a reset and applying the reset to children.
	 * 
	 * @return True if saved.
	 */
	private boolean saveLastReset() {
		if (stateHandler().getState() != ParentState.READY) {
			return true;
		}
		
		try {
			save();
		} catch (ComponentPersistException e) {
			getStateChanger().setStateException(e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Perform a hard reset. The super method is overridden
	 * so as not to reset the child but destroy them.
	 */
	public boolean hardReset() {
		ComponentBoundry.push(loggerName(), this);
		try {
			return stateHandler().waitToWhen(new IsHardResetable(), new Runnable() {
				public void run() {

					logger().debug("Hard Reset requested.");

					if (ourSession == null) {
						lastReset = Reset.HARD;
						if (!saveLastReset()) {
							return;
						}
					}
					else {
						lastReset = null;
					}	

					childStateReflector.stop();
					childHelper.hardResetChildren();
					reset();
					stop = false;
					restored = false;
					getStateChanger().setState(ParentState.READY);
					logger().info("Hard Reset complete.");
				}
			});
		} finally {
			ComponentBoundry.pop();
		}
	}
	
	/**
	 * @return Returns the persister.
	 */
	public OddjobPersister getPersister() {
		return persister;
	}
	
	/**
	 * @param persister The persister to set.
	 */
	public void setPersister(OddjobPersister persister) {
		this.persister = persister;
	}

	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.registry.BeanDirectoryOwner#provideBeanDirectory()
	 */
	@Override
	public BeanDirectory provideBeanDirectory() {
		if (ourSession == null) {
			return null;
		}
		return ourSession.getBeanRegistry();
	}
	
	/**
	 * Getter for last reset.
	 * 
	 * @return
	 */
	public Reset getLastReset() {
		return lastReset;
	}
	
	/*
	 * Custom serialization.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		
		String config = null;
		ConfigurationSession session = provideConfigurationSession();
		if (session != null) {
			DragPoint root = session.dragPointFor(this);
			if (root != null) {
				config = root.copy();
			}
		}
		s.writeObject(config);
	}
	
	/*
	 * Custom serialization.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		String config = (String) s.readObject();
		if (config != null) {
			this.configuration = new XMLConfiguration(
					"Restored Configuration", config);
			this.restored = true;
		}
		completeConstruction();
	}

	/**
	 * @return Returns the args.
	 */
	public String[] getArgs() {
		return args;
	}
	
	/**
	 * @param args The args to set.
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	/**
	 * Getter
	 * 
	 * @param key The key.
	 * 
	 * @return A value.
	 */
	public ArooaValue getExport(String key) {
		return export.get(key);
	}
	
	/**
	 * Setter.
	 * 
	 * @param key
	 * @param value
	 */
	public void setExport(String key, ArooaValue value) {
		if (export == null) {
			export = new LinkedHashMap<String, ArooaValue>();
		}
		if (value == null && export.containsKey(key)) {
			export.remove(key);
		}
		else {
			ComponentBoundry.push(loggerName(), this);
			try {
				logger().debug("Adding value to export: " + 
					key + "=" + value);
				export.put(key, value);
			} finally {
				ComponentBoundry.pop();
			}
		}
	}
	
	/**
	 * Getter.
	 * 
	 * @return Properties, if set, or null.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Setter.
	 * 
	 * @param properties Optional properties.
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Getter.
	 * 
	 * @return Inheritance property.
	 */
	public OddjobInheritance getInheritance() {
		return inheritance;
	}

	/**
	 * Setter.
	 * 
	 * @param inheritance Inheritance property.
	 */
	public void setInheritance(OddjobInheritance inheritance) {
		this.inheritance = inheritance;
	}

	/**
	 * @oddjob.property dir
	 * @oddjob.description The name of the directory the configuration
	 * file is in.
	 * @oddjob.required R/O
	 * 
	 * @return The directory path.
	 */
	public File getDir() {
	    if (file == null) {
	        return null;
	    }
		return file.getAbsoluteFile().getParentFile();
	}

	/**
	 * @oddjob.property version
	 * @oddjob.description This Oddjob's version.
	 * 
	 * @return The version.
	 */
	public String getVersion() {
		return Version.getCurrentVersionAndBuildDate();
	}
	
	/**
	 * The object which is the Oddjob root.
	 *
	 */
	public class OddjobRoot implements Stateful, ServiceProvider, 
			ConfigurationOwner {
		
		private final OddjobServices oddjobServices;
		
		OddjobRoot(OddjobServices services) {
			this.oddjobServices = services;
		}
		
		@Override
		public void addOwnerStateListener(OwnerStateListener listener) {
			Oddjob.this.addOwnerStateListener(listener);
		}
		
		@Override
		public void removeOwnerStateListener(OwnerStateListener listener) {
			Oddjob.this.removeOwnerStateListener(listener);
		}

		@Override
		public SerializableDesignFactory rootDesignFactory() {
			return Oddjob.this.rootDesignFactory();
		}
	
		@Override
		public ArooaElement rootElement() {
			return Oddjob.this.rootElement();
		}
		
		@Override
		public ConfigurationSession provideConfigurationSession() {
			return Oddjob.this.provideConfigurationSession();
		}
		
	    public void setJob(Object child) {
	    	if (child == null) {
			    logger().debug("Removing child.");
				childHelper.removeChildAt(0);
	    	}
	    	else {
			    logger().debug("Adding child [" + child + "]");
			    if (Oddjob.this.childHelper.getChild() != null) {
			        throw new OddjobConfigException(
			                "Oddjob can't have more than one child component.");
			    }
				childHelper.insertChild(0, child);
	    	}
	    }

	    public void addStateListener(StateListener listener) {
	    	Oddjob.this.addStateListener(listener);
	    }
	    
	    public void removeStateListener(StateListener listener) {
	    	Oddjob.this.removeStateListener(listener);
	    }

	    @Override
	    public StateEvent lastStateEvent() {
	    	return Oddjob.this.lastStateEvent();
	    }
	    
		public File getFile() {
			if (file == null) {
				return null;
			}
			return file.getAbsoluteFile();
		}
		
		public File getDir() {
		    if (file == null) {
		        return null;
		    }
			return file.getAbsoluteFile().getParentFile();
		}
	    
		/**
		 * @return Returns the args.
		 */
		public Object[] getArgs() {
			if (args == null) {
				// Oddjob Main will always set an empty String array when
				// no args are passed in. We want the same behaviour when 
				// Oddjob is used embedded.
				return new String[0];
			}
			else {
				return args;
			}
		}

		public Services getServices() {
			return oddjobServices;
		}
		
		public ClassLoader getClassLoader() {
			return oddjobServices.getClassLoader();
		}
	}
	
	/**
	 * Provide an {@link ArooaBeanDescriptor} for the root Oddjob bean.
	 * 
	 */
	public static class OddjobRootArooa implements ArooaBeanDescriptor {

		public ParsingInterceptor getParsingInterceptor() {
			return null;
		}
		
		public String getTextProperty() {
			return null;
		}
		
		public String getComponentProperty() {
			return "job";
		}
		
		public ConfiguredHow getConfiguredHow(String property) {
			return ConfiguredHow.ELEMENT;
		}
		
		public String getFlavour(String property) {
			return null;
		}
		
		public boolean isAuto(String property) {
			return false;
		}
		
		@Override
		public ArooaAnnotations getAnnotations() {
			return new NoAnnotations();
		}
	}
	
	/**
	 * Getter.
	 * 
	 * @return An ArooaDescriptorFactory or null.
	 */
	public ArooaDescriptorFactory getDescriptorFactory() {
		return descriptorFactory;
	}

	/**
	 * Setter.
	 * 
	 * @param descriptorFactory And ArooaDescriptorFactory.
	 */
	public void setDescriptorFactory(ArooaDescriptorFactory descriptorFactory) {
		this.descriptorFactory = descriptorFactory;
	}
	
	/**
	 * Getter for {@link OddjobExecutors} that have been given to this
	 * instance of Oddjob. This getter does not expose the internal executors.
	 * 
	 * @return OddjobExecutors or null.
	 */
	public OddjobExecutors getOddjobExecutors() {
		return oddjobExecutors;
	}

	/**
	 * Setter.
	 * 
	 * @param executors OddjobExecutors
	 */
	public void setOddjobExecutors(OddjobExecutors executors) {
		this.oddjobExecutors = executors;
	}

	/**
	 * Getter for {@link OddjobServices}.
	 * 
	 * @return
	 */
	public OddjobServices getOddjobServices() {
		return oddjobServices;
	}

	/**
	 * Allow for injection of {@link OddjobServices}
	 * 
	 * @param oddjobServices
	 */
	@Inject
	public void setOddjobServices(OddjobServices oddjobServices) {
		this.oddjobServices = oddjobServices;
	}

	@Override
	public String toString() {
		String name = getName();
		if (name != null) {
			return name;
		}
		if (file != null) {
			return "Oddjob " + file.getName();
		}
		return getClass().getSimpleName();
	}	
	
	/**
	 * Wrapper to replace this Oddjob with the root object.
	 */
	class OddjobConfigurationSession implements ConfigurationSession {

		private final ConfigurationSession delegate;
		
		public OddjobConfigurationSession(ConfigurationSession delegate) {
			this.delegate = delegate;
		}
		
		public DragPoint dragPointFor(Object component) {
			// required for the Design Inside action.
			if (component == Oddjob.this) {
				component = Oddjob.this.oddjobRoot;
			}
			return delegate.dragPointFor(component);
		}
		
		public ArooaDescriptor getArooaDescriptor() {
			return delegate.getArooaDescriptor();
		}
		
		public void save() throws ArooaParseException {
			delegate.save();
		}
		
		public boolean isModified() {
			return delegate.isModified();
		}
		
		public void addSessionStateListener(SessionStateListener listener) {
			delegate.addSessionStateListener(listener);
		}
		
		public void removeSessionStateListener(SessionStateListener listener) {
			delegate.removeSessionStateListener(listener);
		}
	}

	/**
	 * Getter.
	 * 
	 * @return An InputHandler.
	 */
	public InputHandler getInputHandler() {
		return inputHandler;
	}

	/**
	 * Setter.
	 * 
	 * @param inputHandler An InputHandler.
	 */
	public void setInputHandler(InputHandler inputHandler) {
		this.inputHandler = inputHandler;
	}		
}
