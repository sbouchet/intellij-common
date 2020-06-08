/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.common.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.tree.TreePath;
import java.awt.Component;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class TreeAction extends AnAction {

    private Class[] filters;
    private boolean acceptMultipleItems;

    public TreeAction(Class... filters) {
        this.acceptMultipleItems = false;
        this.filters = filters;
    }

    public TreeAction(boolean acceptMultipleItems, Class... filters) {
        this.acceptMultipleItems = acceptMultipleItems;
        this.filters = filters;
    }

    protected Tree getTree(AnActionEvent e) {
        return (Tree) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    }

    protected Object getSelected(Tree tree) {
        return tree.getSelectionModel().getSelectionPath().getLastPathComponent();
    }

    protected Object[] getSelectedNodes(Tree tree) {
        TreePath[] treePaths = tree.getSelectionModel().getSelectionPaths();
        return Arrays.stream(treePaths).map(path -> path.getLastPathComponent()).toArray(Object[]::new);
    }

    /**
     * Allows to adjust the selected user object for models that users intermediate user object
     * (see {@link com.intellij.ui.tree.StructureTreeModel}
     *
     * @param selected the original selected user object
     * @return the real user object
     */
    protected Object adjust(Object selected) {
        return selected;
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        Component comp = getTree(e);

        if (comp instanceof Tree) {
            visible = isVisible(getSelectedNodes((Tree) comp));
        }
        e.getPresentation().setVisible(visible);
    }

    public boolean isVisible(Object selected) {
        return Stream.of(filters).anyMatch(cl -> cl.isAssignableFrom(selected.getClass()));
    }

    public boolean isVisible(Object[] selected) {
        if (!acceptMultipleItems && selected.length > 1) {
            return false;
        }

        for (Object item: selected) {
            if (Stream.of(filters).noneMatch(cl -> cl.isAssignableFrom(adjust(item).getClass()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Tree tree = getTree(anActionEvent);
        TreePath[] selectedPaths = tree.getSelectionModel().getSelectionPaths();
        Object[] selected = getSelectedNodes(tree);
        actionPerformed(anActionEvent, selectedPaths, selected);
    }

    public abstract void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected);

    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected) {
        actionPerformed(anActionEvent, path[0], selected[0]);
    }
}
