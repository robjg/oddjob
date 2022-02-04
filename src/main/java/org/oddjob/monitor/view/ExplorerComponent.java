package org.oddjob.monitor.view;

import org.oddjob.arooa.design.designer.ArooaTransferHandler;
import org.oddjob.arooa.design.designer.ArooaTree;
import org.oddjob.arooa.design.view.TreeChangeFollower;
import org.oddjob.arooa.design.view.TreePopup;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.monitor.control.DetailController;
import org.oddjob.monitor.control.NodeControl;
import org.oddjob.monitor.control.PropertyPolling;
import org.oddjob.monitor.model.*;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Objects;

/**
 * The tree view.
 *
 * @author Rob Gordon
 */

public class ExplorerComponent extends JPanel {
    private static final long serialVersionUID = 0;

    private final JTree tree;

    private MonitorMenuBar menuBar;

    private final DetailModel detailModel;

    private final ExplorerModel explorerModel;

    private final PropertyPolling propertyPolling;

    private final JSplitPane split;

    private final TreeChangeFollower treeChangeFollower;

    /**
     * Constructor. Create the main Explorer Component.
     *
     * @param explorerModel   The Model for this explorer component.
     * @param propertyPolling The property polling.
     */
    public ExplorerComponent(ExplorerModel explorerModel,
                             PropertyPolling propertyPolling) {
        this.propertyPolling = Objects.requireNonNull(propertyPolling);
        this.explorerModel = Objects.requireNonNull(explorerModel);

        // create the only detail model. At the moment this component
        // can only show the detail on one node at a time but the
        // model is written so that this could change.
        detailModel = new DetailModel();
        DetailView detailView = new DetailView(detailModel);

        propertyPolling.setPropertyModel(detailModel.getPropertyModel());
        detailModel.addPropertyChangeListener(
                propertyPolling);

        DetailController detailControl = new DetailController(detailModel, detailView);

        JobTreeModel treeModel = new JobTreeModel();
        JobTreeNode rootTreeNode = new JobTreeNode(explorerModel, treeModel);
        treeModel.setRootTreeNode(rootTreeNode);

        tree = new ArooaTree(treeModel) {
            private static final long serialVersionUID = 200801150100L;

            @Override
            public DragPoint getDragPoint(Object treeNode) {
                JobTreeNode jobTreeNode = (JobTreeNode) treeNode;

                ConfigContextSearch search = new ConfigContextSearch();
                return search.dragPointFor(
                        jobTreeNode.getExplorerContext());
            }
        };

        NodeControl nodeControl = new NodeControl();
        tree.addTreeWillExpandListener(nodeControl);

        tree.setShowsRootHandles(true);

        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(detailControl);

        tree.setCellRenderer(new JobTreeCellRenderer());
        ToolTipManager.sharedInstance().registerComponent(tree);

        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);

        treeChangeFollower = new TreeChangeFollower(tree);

        tree.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                tree.removeAncestorListener(this);
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }

            @Override
            public void ancestorAdded(AncestorEvent event) {

                // This must happen after the bindTo method has been called.
                tree.setSelectionPath(new TreePath(tree.getModel().getRoot()));

                tree.requestFocusInWindow();
            }
        });

        ArooaTransferHandler transferHandler = new ArooaTransferHandler();
        transferHandler.addTransferEventListener(
                (event, message, exception) -> {
                    String text = message + "\n" + "Cause: " + exception.getMessage();
                    JOptionPane.showMessageDialog(
                            tree,
                            text,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                });

        tree.setTransferHandler(transferHandler);

        // Order is important here otherwise the child is show without firing
        // the WillExpand listener - Don't know why.
        rootTreeNode.setVisible(true);

        setLayout(new BorderLayout());
        JScrollPane treeScroll = new JScrollPane();

        treeScroll.setViewportView(tree);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treeScroll, detailView);

        add(split);

    }

    /**
     * Beind the Explorer Component to the menu bar.
     *
     * @param monitorMenuBar The menu bar.
     */
    public void bindTo(MonitorMenuBar monitorMenuBar) {
        if (this.menuBar != null) {
            throw new IllegalStateException("MonitorMenuBar already bound.");
        }

        this.menuBar = monitorMenuBar;

        ExplorerJobActions jobActions = new ExplorerJobActions(
                explorerModel.getExplorerActions());

        menuBar.setSession(jobActions, detailModel);

        jobActions.addKeyStrokes(this);

        new TreePopup(tree, menuBar);
    }

    /**
     * Clear up the component.
     */
    public void destroy() {
        detailModel.removePropertyChangeListener(
                propertyPolling);
        treeChangeFollower.close();
    }

    public void balance() {
        split.setDividerLocation((int) (0.33 * split.getPreferredSize().getWidth()));
    }

    public JTree getTree() {
        return tree;
    }
}
