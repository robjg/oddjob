package org.oddjob.jmx;


import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.design.designer.ArooaContainer;
import org.oddjob.arooa.design.designer.ArooaTransferHandler;
import org.oddjob.arooa.design.designer.ArooaTree;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.state.StateConditions;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oddjob.OddjobMatchers.statefulIs;

/**
 * Tests where the dragged component has an Id to find a bug where the Id was being changed to id2 because
 * the cut and pasted wasn't happening in a {@link org.oddjob.arooa.parsing.DragTransaction}.
 */
class ServerDrag2Test {

    @Test
    void draggingWithinOddjob() throws Exception {

        File serverConfig = new File(Objects.requireNonNull(
                getClass().getResource("ServerDrag2WithinOddjob.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(serverConfig);

        oddjob.run();

        assertThat(oddjob, statefulIs(StateConditions.STARTED));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        PretendDragDrop dragDrop = new PretendDragDrop();

        ConfigurationOwner configurationOwner = lookup.lookup("client/jobs",
                ConfigurationOwner.class);

        dragDrop.moveFrom(configurationOwner, lookup.lookup("client/jobs/foo"));

        Object destination = lookup.lookup("client/jobs/sequential");

        boolean result = dragDrop.moveTo(configurationOwner, destination, -1);

        assertThat(result, is(true));

        String configAfter = configurationOwner.provideConfigurationSession().dragPointFor(destination).copy();

        String expected = "<sequential id='sequential'><jobs><echo id='foo'/></jobs></sequential>";

        Diff diff = DiffBuilder.compare(expected)
                .withTest(configAfter).ignoreWhitespace()
                .build();

        assertThat(diff.toString(), diff.hasDifferences(), is(false));

        oddjob.stop();
        oddjob.destroy();
    }

    @Test
    void draggingFromClientToServer() throws Exception {

        File serverConfig = new File(Objects.requireNonNull(
                getClass().getResource("ServerDrag2FromClient.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(serverConfig);

        oddjob.run();

        assertThat(oddjob, statefulIs(StateConditions.STARTED));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        PretendDragDrop dragDrop = new PretendDragDrop();

        dragDrop.moveFrom(oddjob, lookup.lookup("foo"));

        ConfigurationOwner serverOwner = lookup.lookup("client/jobs",
                ConfigurationOwner.class);

        Object destination = lookup.lookup("client/jobs/sequential");

        boolean result = dragDrop.moveTo(serverOwner, destination, -1);

        assertThat(result, is(true));

        String configAfter = lookup.lookup("jobs", ConfigurationOwner.class)
                .provideConfigurationSession()
                .dragPointFor(lookup.lookup("jobs/sequential"))
                .copy();

        String expected = "<sequential id='sequential'><jobs><echo id='foo'/></jobs></sequential>";

        Diff diff = DiffBuilder.compare(expected)
                .withTest(configAfter).ignoreWhitespace()
                .build();

        assertThat(diff.toString(), diff.hasDifferences(), is(false));

        oddjob.stop();
        oddjob.destroy();
    }

    @Test
    void draggingBackToSamePlace() throws Exception {

        File serverConfig = new File(Objects.requireNonNull(
                getClass().getResource("ServerDrag2ToSamePlace.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(serverConfig);

        oddjob.run();

        assertThat(oddjob, statefulIs(StateConditions.STARTED));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        PretendDragDrop dragDrop = new PretendDragDrop();

        ConfigurationOwner configurationOwner = lookup.lookup("client/jobs",
                ConfigurationOwner.class);

        dragDrop.moveFrom(configurationOwner, lookup.lookup("client/jobs/foo"));

        Object destination = lookup.lookup("client/jobs/sequential");

        boolean result = dragDrop.moveTo(configurationOwner, destination, -1);

        assertThat(result, is(true));

        String configAfter = lookup.lookup("jobs", ConfigurationOwner.class).provideConfigurationSession()
                .dragPointFor(lookup.lookup("jobs/sequential"))
                .copy();

        String expected = "<sequential id='sequential'><jobs><echo id='foo'/></jobs></sequential>";

        Diff diff = DiffBuilder.compare(expected)
                .withTest(configAfter).ignoreWhitespace()
                .build();

        assertThat(diff.toString(), diff.hasDifferences(), is(false));

        oddjob.stop();
        oddjob.destroy();
    }


    static class OurTransferHandler extends ArooaTransferHandler {

        @Override
        protected Transferable createTransferable(JComponent c) {
            return super.createTransferable(c);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
        }
    }

    static class OurDragContainer extends JComponent implements ArooaContainer {

        private final DragPoint dragPoint;

        OurDragContainer(DragPoint dragPoint) {
            this.dragPoint = dragPoint;
        }

        @Override
        public DragPoint getCurrentDragPoint() {
            return dragPoint;
        }

        @Override
        public DropPoint dropPointFrom(TransferHandler.TransferSupport support) {
            throw new IllegalStateException("Unexpected.");
        }
    }

    static class OurDropContainer extends ArooaTree {

        private final DragPoint dragPoint;

        private final int index;

        OurDropContainer(DragPoint dragPoint, int index) {
            this.dragPoint = dragPoint;
            this.index = index;
        }

        @Override
        public DragPoint getCurrentDragPoint() {
            throw new IllegalStateException("Unexpected.");
        }

        @Override
        protected DragPoint getDragPoint(Object treeNode) {
            return dragPoint;
        }

        @Override
        public DropPoint dropPointFrom(TransferHandler.TransferSupport support) {
            return new DropPoint() {
                @Override
                public int getIndex() {
                    return index;
                }

                @Override
                public DragPoint getDragPoint() {
                    return dragPoint;
                }
            };
        }


    }

    static class PretendDragDrop {

        OurTransferHandler transferHandler = new OurTransferHandler();

        JComponent dragContainer;

        Transferable transferable;

        void moveFrom(ConfigurationOwner owner, Object node) {

            dragContainer = new OurDragContainer(
                    owner.provideConfigurationSession().dragPointFor(node));
            dragContainer.setTransferHandler(transferHandler);

            transferable = transferHandler.createTransferable(dragContainer);
        }

        boolean moveTo(ConfigurationOwner owner, Object node, int index) {

            JComponent dragContainer = Objects.requireNonNull(this.dragContainer,
                    "You need to move from first.");

            OurDropContainer dropContainer = new OurDropContainer(
                    owner.provideConfigurationSession().dragPointFor(node), index);
            dropContainer.setTransferHandler(transferHandler);

            DropTarget dropTarget = mock(DropTarget.class);

            DropTargetContext dropTargetContext = mock(DropTargetContext.class);
            when(dropTargetContext.getDropTarget()).thenReturn(dropTarget);
            when(dropTargetContext.getComponent()).thenReturn(dropContainer);

            Point point = mock(Point.class);

            DropTargetDragEvent dragEvent = new DropTargetDragEvent(dropTargetContext, point,
                    DnDConstants.ACTION_MOVE, DnDConstants.ACTION_MOVE) {
                @Override
                public boolean isDataFlavorSupported(DataFlavor df) {
                    return true;
                }

                @Override
                public Transferable getTransferable() {
                    return transferable;
                }

            };

            AtomicBoolean result = new AtomicBoolean();


            DropTargetDropEvent dropEvent = new DropTargetDropEvent(dropTargetContext, point,
                    DnDConstants.ACTION_MOVE, DnDConstants.ACTION_MOVE) {

                @Override
                public boolean isDataFlavorSupported(DataFlavor df) {
                    return true;
                }

                @Override
                public Transferable getTransferable() {
                    return transferable;
                }

                @Override
                public void dropComplete(boolean success) {
                    result.set(success);
                }
            };


            TransferHandler.TransferSupport transferSupport = new TransferHandler.TransferSupport(dropContainer, transferable);

            // Sets stuff in the transfer handler.
            dropContainer.getDropTarget().dragEnter(dragEvent);

            // Will call import data on target.
            dropContainer.getDropTarget().drop(dropEvent);

            // I can't work out how to get access to the dragDropEnd() - it's invoked from the
            // DragSourceContext which seems way to complicated to mock.
            if (result.get()) {
                transferHandler.exportDone(dragContainer, transferable, TransferHandler.MOVE);
            }

            return result.get();
        }
    }

}
