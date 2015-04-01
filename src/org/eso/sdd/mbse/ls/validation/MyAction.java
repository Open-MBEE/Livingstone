/**
 * $Id: $
 */
package org.eso.sdd.mbse.ls.validation;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Test action.
 *
 * @author Rimvydas Vaidelis
 * @version $Revision: $, $Date: $
 */
public class MyAction extends NMAction
{
    public MyAction(String id, String name, KeyStroke stroke, String group)
    {
        super(id, name, stroke, group);
    }

    public MyAction(String id, String name, KeyStroke stroke)
    {
        super(id, name, stroke);
    }

    public MyAction(String id, String name, int mnemonic, String group)
    {
        super(id, name, mnemonic, group);
    }

    public MyAction(String id, String name, int mnemonic)
    {
        super(id, name, mnemonic);
    }

    @Override
	public void actionPerformed(ActionEvent e)
    {
        JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), "Action " + getName() + " performed!");
    }
}
