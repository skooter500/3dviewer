package com.michal.debski.environment;

import com.michal.debski.*;
import com.michal.debski.loader.Loader;
import com.michal.debski.utilities.Colour;

import com.michal.debski.utilities.PanelUtility;
import com.michal.debski.utilities.mdTimer;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

import static org.lwjgl.opengl.GL30.*;

public class Light extends Model
{
    // This should be the parent class. Child classes: directional light, point light
    // Light method render should be called before any drawing, so it will set boolean variable
    // that manages lighting in shader to true, so the scene will be drawn with lighting.
    protected float strength;
    protected boolean lightActive = true;
    private boolean orbiting = false;
    private Vector3f orbitingPosition = null;
    private float orbitingRadius = 10.f;
    private float orbitingSpeed = 2.f;

    private boolean renderLightCube = true;
    private static boolean castShadows = true;
    private Shadows shadows = null;
    private mdTimer orbitingTimer = new mdTimer();


    public Light(Vector3f position)
    {
        super("Light", Loader.PrimitiveType.Cube);
        setColor(new Colour(1.f));
        this.getTransform().setPosition(position);
        shadows = new Shadows(this.getTransform().getPosition());
    }

    public Light(Vector3f position, Colour color)
    {
        super("Light", Loader.PrimitiveType.Cube);
        this.getTransform().setPosition(position);
        setColor(color);
        shadows = new Shadows(this.getTransform().getPosition());
    }

    public void on()
    {
        this.lightActive = true;
    }

    public void off()
    {
        this.lightActive = false;
    }

    public void setOribitng(boolean orbiting)
    {
        this.orbiting = orbiting;
        if(orbiting) {
            orbitingTimer.start();
            orbitingRadius = new Vector2f().distance(new Vector2f(getTransform().getPosition().x, getTransform().getPosition().z));
        }
        else {
            orbitingTimer.stop();
        }
    }

    public void setOrbitingSpeed(float orbitingSpeed)
    {
        this.orbitingSpeed = orbitingSpeed;
    }

    public void setOrbitingRadius(float orbitingRadius)
    {
        this.orbitingRadius = orbitingRadius;
    }

    public void setOrbitingAroundPosition(Vector3f orbitingPosition, float orbitingRadius, float orbitingSpeed, boolean orbiting)
    {
        setOribitng(orbiting);
        this.orbitingRadius = orbitingRadius;
        this.orbitingSpeed = orbitingSpeed;
        this.orbitingPosition = orbitingPosition;
    }

    public void renderLightCube(boolean val)
    {
        this.renderLightCube = val;
    }

    public void Render(Vector3f cameraPosition)
    {
        ShaderManager.GetShader().use();
        renderLight();
        if(lightActive)
        {

            /*ShaderManager.GetShader().setVec3("light.direction", getTransform().getPosition());
            //ShaderManager.GetShader().setVec3("lightPos", position);
            ShaderManager.GetShader().setVec3("light.color", color.r, color.g, color.b);*/
        }
    }

    protected void renderLight()
    {
        if(renderLightCube)
        {
            ShaderManager.GetShader().setBool("lightActive", false);
            if(orbiting)
            {
                getTransform().getPosition().x = (float)Math.sin(orbitingTimer.getCurrentTime() * orbitingSpeed) * orbitingRadius;
                getTransform().getPosition().z = (float)Math.cos(orbitingTimer.getCurrentTime() * orbitingSpeed) * orbitingRadius;
            }

            Render();
        }

        ShaderManager.GetShader().setBool("lightActive", lightActive);
    }

    public void renderSceneWithShadows(SceneInterface scene)
    {
        ShaderManager.GetShader().use();
        ShaderManager.GetShader().setBool("shadowsActive", castShadows);
        ShaderManager.GetShader().setBool("lightActive", lightActive);

        if(castShadows)
        {
            Shader tempShader = ShaderManager.GetShader();
            shadows.fillDepthMap(scene);

            ShaderManager.SetShader(tempShader);

            // Render scene as normal

            ShaderManager.GetShader().setInt("shadows.depthMap", 6);
            ShaderManager.GetShader().setMat4("lightSpaceMatrix", shadows.getLightSpaceMatrix());
            //ShaderManager.GetShader().setBool("shadows.switchedOn", true);
            glActiveTexture(shadows.shaderTextureNum);
            glBindTexture(GL_TEXTURE_2D, shadows.getDepthMap());
        }
    }

    public static void CastShadows(boolean cs)
    {
        castShadows = cs;
    }

    public static boolean ShadowsEnabled()
    {
        return castShadows;
    }

    @Override
    public PanelEntity createPanelEntity()
    {
        JPanel panel = new JPanel();
        String panelName = "Light";
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Get panel with transform settings
        JPanel transformPanel = getTransform().createPanelEntity().getPanel();

        JPanel orbitingPanel = new JPanel();
        orbitingPanel.setLayout(new GridBagLayout());


        JPanel environmentPanel = new JPanel();
        environmentPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.1;
        gbc.insets = new Insets(2,2,5,2);


        JLabel radiusLabel = new JLabel(String.format("%-2.2f", orbitingRadius));
        radiusLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        radiusLabel.setPreferredSize(new Dimension(20, 20));


        int sliderWidth = 200;
        int sliderHeight = 50;
        JSlider radiusSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, (int)orbitingRadius);
        radiusSlider.addChangeListener(e -> {
            orbitingRadius = radiusSlider.getValue();
            radiusLabel.setText(String.format("%-2.2f", orbitingRadius));
        });


        JLabel speedLabel = new JLabel(String.format("%-2.2f", orbitingSpeed));
        speedLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        speedLabel.setPreferredSize(new Dimension(20, 20));

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, (int)orbitingSpeed * 10);
        speedSlider.addChangeListener(e -> {
            orbitingSpeed = (float)speedSlider.getValue() / 10.f;
            speedLabel.setText(String.format("%-2.2f", orbitingSpeed));
        });


        JCheckBox orbitingCheckBox = new JCheckBox("Is orbiting");
        orbitingCheckBox.setSelected(this.orbiting);
        PanelUtility.SetPanelEnabled(transformPanel, !this.orbiting);
        radiusSlider.setEnabled(this.orbiting);
        speedSlider.setEnabled(this.orbiting);
        orbitingCheckBox.addItemListener(e -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                setOribitng(true);
                PanelUtility.SetPanelEnabled(transformPanel, false);
                radiusSlider.setEnabled(true);
                speedSlider.setEnabled(true);
            }
            else {
                setOribitng(false);
                PanelUtility.SetPanelEnabled(transformPanel, true);
                radiusSlider.setEnabled(false);
                speedSlider.setEnabled(false);
            }
        });


        JLabel label = new JLabel("Orbiting radius", JLabel.CENTER);
        gbc.gridx = 1;
        gbc.gridy = 0;
        orbitingPanel.add(label, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        label = new JLabel("Value");
        orbitingPanel.add(label, gbc);
        gbc.gridx++;
        orbitingPanel.add(radiusSlider, gbc);
        gbc.gridx++;
        orbitingPanel.add(radiusLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy++;
        label = new JLabel("Orbiting speed", JLabel.CENTER);
        orbitingPanel.add(label, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        label = new JLabel("Value");
        orbitingPanel.add(label, gbc);
        gbc.gridx++;
        orbitingPanel.add(speedSlider, gbc);
        gbc.gridx++;
        orbitingPanel.add(speedLabel, gbc);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy++;
        orbitingPanel.add(orbitingCheckBox, gbc);

        orbitingPanel.setMaximumSize(new Dimension(Gui.GetWidth(), 160));
        orbitingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        orbitingPanel.setBorder(BorderFactory.createTitledBorder("Orbiting"));

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.1;
        gbc.gridy = 0;
        gbc.insets = new Insets(2,2,5,2);
        //gbc.gridwidth = 2;
        JCheckBox shadowsCheckBox = new JCheckBox("Cast Shadows");
        shadowsCheckBox.setSelected(this.castShadows);
        shadowsCheckBox.addItemListener(e -> {
            castShadows = shadowsCheckBox.isSelected();
        });

        JCheckBox lightCheckBox = new JCheckBox("Cast Lights");
        lightCheckBox.setSelected(this.lightActive);
        lightCheckBox.addItemListener(e -> {
            lightActive = lightCheckBox.isSelected();
            shadowsCheckBox.setSelected(lightCheckBox.isSelected() && castShadows);
        });


        environmentPanel.add(shadowsCheckBox, gbc);
        gbc.gridy++;
        environmentPanel.add(lightCheckBox, gbc);

        environmentPanel.setMaximumSize(new Dimension(Gui.GetWidth(), 100));
        environmentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        environmentPanel.setBorder(BorderFactory.createTitledBorder("Environment"));

        // Create color picker panel
        JPanel colorPanel = getColor().createPanelEntity().getPanel();

        panel.add(transformPanel);
        panel.add(colorPanel);
        panel.add(orbitingPanel);
        panel.add(environmentPanel);


        return new PanelEntity(panel, panelName, false, false);
    }
}
