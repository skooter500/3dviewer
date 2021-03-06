/* Date: 03/04/2019
 * Developer: Michal Debski
 * Github: github.com/debson
 * Class description:   Colour class is used to hold RGBA colors;
 *                      Colour's GUI is a modified version of a JColorChooser component.
 *
 */

package com.michal.debski.utilities;

import com.michal.debski.Gui;
import com.michal.debski.Panel;
import com.michal.debski.PanelEntity;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;

public class Colour implements Panel
{
    public float r, g, b, a;

    public Colour(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Colour(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1.f;
    }

    public Colour(float initValue)
    {
        this.r = initValue;
        this.g = initValue;
        this.b = initValue;
        this.a = initValue;
    }

    @Override
    public PanelEntity createPanelEntity()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Create color chooser
        JColorChooser jcc = new JColorChooser(new java.awt.Color(255, 255, 255, 255));
        jcc.getSelectionModel().addChangeListener(e -> {
            java.awt.Color newColor = jcc.getColor();
            r = (float)newColor.getRed() / 255.f;
            g = (float)newColor.getGreen() / 255.f;
            b = (float)newColor.getBlue() / 255.f;
        });

        // Remove preview panel
        jcc.setPreviewPanel(new JPanel());

        // Leave only RGB component
        AbstractColorChooserPanel[] panels = jcc.getChooserPanels();
        for(AbstractColorChooserPanel accp : panels)
        {
            if(accp.getDisplayName().equals("RGB") == false)
            {
                jcc.removeChooserPanel(accp);
            }
            else
            {
                // Hacky way of removing sliders(there is no other way TBH)
                accp.remove(1);
                JPanel p2 = (JPanel) accp.getComponent(0);
                // Remove 9 components(sliders and their lables...)
                for(int i = 0; i < 9; i++)
                    p2.remove(0);
            }
        }

        panel.add(jcc);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createTitledBorder("Colour"));
        panel.setMaximumSize(new Dimension(Gui.GetWidth(), 250));

        return new PanelEntity(panel, "ColorPicker", false, false);
    }
}
