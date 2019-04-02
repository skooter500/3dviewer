package com.michal.debski;

import com.michal.debski.environment.DirectionalLight;
import com.michal.debski.environment.Light;
import com.michal.debski.loader.Loader;
import com.michal.debski.utilities.Colour;

import org.joml.Vector2f;
import org.joml.Vector3f;


public class Scene implements GameHandlerInterface, SceneInterface
{
    private Shader shader;
    private Camera camera;
    private DirectionalLight dirLight;
    private float cameraMoveSpeed = 10.f;
    private Model myModel, floor, cube;

    private Gui gui;


    @Override
    public void OnWindowOpen()
    {
        shader = new Shader("shaders/default.vert", "shaders/default.frag");


        camera = new Camera(new Vector2f(
                WindowProperties.getWidth(),
                WindowProperties.getHeight()),
                new Vector3f(0.f, 15.f, 30.f));


        // Remember to use "\\" directory delimiter! Otherwise there will be an error.
        String path = "assets/teapot.obj";
        String path2 = "assets/nanosuit/nanosuit.obj";
        String path3 = "assets/teddybear.obj";
        String path4 = "assets/head.obj";
        String path5 = "assets/wolf.obj";

        myModel = new Model(path2);
        myModel.getTransform().setPosition(new Vector3f(0.f, 0.f, 0.f));
        myModel.getTransform().setScale(new Vector3f(1.f));
        //myModel.setColor(new Colour(1.f, 0.5f, 1.f));
        floor = new Model("Floor", Loader.PrimitiveType.Plane);
        floor.setColor(new Colour(1.f, 0.f, 1.f, 1.f));

        cube = new Model("Cube", Loader.PrimitiveType.Cube);
        //cube.setScale(new Vector3f(50.f, 1.f, 50.f));
        cube.getTransform().setPosition(new Vector3f(0.f, 1.f, 10.f));

        //camera.lockCameraAt(myModel.getTransform().getPosition(), true);

        dirLight = new DirectionalLight(new Vector3f( -20.f, 30.f, -30.f), new Colour( 1.f));
        dirLight.getTransform().setScale(new Vector3f(3.f));


        gui = new Gui();

        gui.createGui(Containers.panelContainer);
    }

    @Override
    public void OnWindowClose()
    {
        if(gui != null)
            gui.dispose();
    }

    @Override
    public void OnNewFrame()
    {

    }

    @Override
    public void OnFinishFrame()
    {

    }

    @Override
    public void OnRealtimeUpdate()
    {
        processCameraInput();

    }

    @Override
    public void OnRealTimeRender()
    {
        // Set global shader to default shader
        ShaderManager.SetShader(shader);
        updateMatrices(shader);

        // Render scene to depth map
        dirLight.renderSceneWithShadows(this);
        // Render light box and set light uniforms
        dirLight.Render(camera.transform.getPosition());
        // Render scene as normal
        renderScene(shader);

    }

    @Override
    public void OnFileDrop(String pathOfDroppedFile)
    {
        myModel = null;
        myModel = new Model(pathOfDroppedFile);
        gui.replaceModel();
    }

    @Override
    public void OnWindowMove(int winX, int winY)
    {
        if(gui != null)
            gui.setPosition();
    }

    @Override
    public void OnWindowFocus(boolean hasFocus)
    {
        // Small hack, so the gui window will always be visible only when game window is visible
        gui.setAlwaysOnTop(hasFocus);
    }

    @Override
    public void updateMatrices(Shader shader)
    {
        shader.use();
        shader.setMat4("projection", camera.getProjectionMatrix());
        shader.setMat4("view", camera.getViewMatrix());
    }

    private void processCameraInput()
    {
        Vector2f relMousePos = Input.GetRelativeMousePos();

        if(Input.IsKeyDown(Keycode.MouseMiddle))
            camera.processMouseMovement(relMousePos.x, relMousePos.y);

        float speed = cameraMoveSpeed;

        if(Input.IsKeyDown(Keycode.LShift))
            speed *= 3.f;

        if(Input.IsKeyDown(Keycode.W))
            camera.processKeyboard(Camera.CameraMovement.Forward, (float)Time.deltaTime, speed);
        if(Input.IsKeyDown(Keycode.S))
            camera.processKeyboard(Camera.CameraMovement.Backward, (float)Time.deltaTime, speed);
        if(Input.IsKeyDown(Keycode.A))
            camera.processKeyboard(Camera.CameraMovement.Left, (float)Time.deltaTime, speed);
        if(Input.IsKeyDown(Keycode.D))
            camera.processKeyboard(Camera.CameraMovement.Right, (float)Time.deltaTime, speed);
    }

    @Override
    public void renderScene(Shader shader)
    {
        floor.Render();
        myModel.Render();
        cube.Render();
    }

    public static void main(String[] args)
    {
        Scene scene = new Scene();
        Game game = new Game(scene);

        game.Open();
        game.Run();
        game.Close();
    }
}
