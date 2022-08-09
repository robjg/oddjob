package org.oddjob.monitor.action;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.input.StdInInputHandler;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextInitialiser;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.state.ParentState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DesignerActionTest extends OjTestCase {

    @Test
    public void testRoot() {

        ExplorerContext rootContext = mock(ExplorerContext.class);
        when(rootContext.getThisComponent()).thenReturn("SomeComp");
        when(rootContext.getParent()).thenReturn(null);

        DesignerAction test = new DesignerAction();

        test.setSelectedContext(rootContext);
        test.prepare();

        assertFalse(test.isVisible());
        assertFalse(test.isEnabled());

        test.setSelectedContext(null);

        assertFalse(test.isVisible());
        assertFalse(test.isEnabled());

        test.setSelectedContext(null);
    }







    private static class ParentContext extends MockExplorerContext {

        ConfigurationOwner configOwner;

        @Override
        public Object getValue(String key) {
            assertEquals(ConfigContextInitialiser.CONFIG_OWNER, key);
            return configOwner;
        }
    }

    private static class OurExplorerContext extends MockExplorerContext {

        Object component;

        ParentContext parent = new ParentContext();

        @Override
        public Object getThisComponent() {
            return component;
        }

        @Override
        public ExplorerContext getParent() {
            return parent;
        }
    }

    XMLConfiguration config;

    DesignerAction test = new DesignerAction();


    @Test
    public void testGoodConfig() {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <input id='sequential'/>" +
                        " </job>" +
                        "</oddjob>";

        config = new XMLConfiguration("TEST2", xml);

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(config);
        oddjob.setInputHandler(new StdInInputHandler());
        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        Object sequentialJob = new OddjobLookup(oddjob).lookup("sequential");

        OurExplorerContext explorerContext =
                new OurExplorerContext();
        explorerContext.parent.configOwner = oddjob;
        explorerContext.component = sequentialJob;

        test.setSelectedContext(explorerContext);
        test.prepare();

        assertTrue(test.isEnabled());

        Form form = test.form();

        assertNotNull(form);
    }

    public static void main(String... args) throws Exception {

        final DesignerActionTest test = new DesignerActionTest();
        test.testGoodConfig();

        test.config.setSaveHandler(System.out::println);

        Component view = SwingFormFactory.create(Objects.requireNonNull(test.test.form())).dialog();

        KeyboardFocusManager focusManager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(evt -> System.out.println(evt.getPropertyName() + "=" +
                evt.getNewValue()));

        JFrame frame = new JFrame();
        frame.getContentPane().add(view);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                try {
                    test.test.action();
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

}

