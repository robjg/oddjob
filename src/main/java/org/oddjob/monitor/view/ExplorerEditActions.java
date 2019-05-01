package org.oddjob.monitor.view;

import org.oddjob.arooa.design.actions.EditActionsContributor;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextSearch;
import org.oddjob.monitor.model.SelectedContextAware;

/**
 * A Set of edit actions that contribute to the menu and become
 * enabled/disabled dependent on job selection.
 * <p>
 * The actions forward to the actual edit actions in the underlying
 * components action map (in explorers case, the trees action map).
 * Disabling/Enabling is not passed to the underlying actions
 * so they will always respond to key strokes.
 *
 * @author rob
 */
public class ExplorerEditActions extends EditActionsContributor
        implements SelectedContextAware {

    private ExplorerContext context;

    public void setSelectedContext(ExplorerContext context) {
        this.context = context;

        if (context == null) {
            setCutEnabled(false);
            setCopyEnabled(false);
            setPasteEnabled(false);
            setDeleteEnabled(false);
        }
    }

    @Override
    public void prepare() {

        DragPoint dragPoint;

        ConfigContextSearch search = new ConfigContextSearch();
        dragPoint = search.dragPointFor(context);

        if (dragPoint == null) {
            setCutEnabled(false);
            setCopyEnabled(false);
            setPasteEnabled(false);
            setDeleteEnabled(false);
        } else {
            if (!dragPoint.supportsCut()) {
                setCutEnabled(false);
                setDeleteEnabled(false);
            } else {
                setCutEnabled(true);
                setDeleteEnabled(true);
            }

            setCopyEnabled(true);

            if (dragPoint.supportsPaste()) {
                setPasteEnabled(true);
            } else {
                setPasteEnabled(false);
            }
        }
    }
}
