package com.plainston.gtquality.integration.blockrenderer;

import static com.plainston.gtquality.integration.blockrenderer.GT6MultiblockHandler.BUTTON_Y;
import static com.plainston.gtquality.integration.blockrenderer.GT6MultiblockHandler.SCENE_HEIGHT;
import static com.plainston.gtquality.integration.blockrenderer.GT6MultiblockHandler.SCENE_Y;
import static com.plainston.gtquality.integration.blockrenderer.GT6MultiblockHandler.WIDTH;
import static com.plainston.gtquality.integration.blockrenderer.GT6MultiblockHandler.translate;

import java.awt.Point;

import net.minecraft.util.MovingObjectPosition;

import org.joml.Vector3f;
import org.lwjgl.input.Mouse;

import blockrenderer6343.client.renderer.ImmediateWorldSceneRenderer;

final class MultiblockScene {

    private final PreviewWorld world = new PreviewWorld();
    private final ImmediateWorldSceneRenderer renderer = new ImmediateWorldSceneRenderer(world);
    private final Vector3f center = new Vector3f();
    private final Vector3f defaultCenter = new Vector3f();
    private float yaw, pitch, distance, defaultDistance;
    private int layer = -1, layers;
    private int dragButton = -1;
    private Point previousMouse;

    void load(GT6MultiblockCatalog.Page page) {
        renderer.world.clear();
        page.populate(renderer.world);
        Vector3f size = renderer.world.getSize();
        center.set(renderer.world.getMinPos())
            .add(size.x / 2, size.y / 2, size.z / 2);
        defaultCenter.set(center);
        layers = (int) size.y;
        defaultDistance = Math.max(5, Math.max(size.x, Math.max(size.y, size.z)) * 2.1f);
        reset();
        renderer.setOnLookingAt(hit -> {});
    }

    private void reset() {
        yaw = -45;
        pitch = 25;
        distance = defaultDistance;
        center.set(defaultCenter);
        layer = -1;
        dragButton = -1;
        previousMouse = null;
        renderer.setRenderAllBlocks();
    }

    void draw(int left, int top, Point mouse) {
        if (dragButton >= 0) {
            if (!Mouse.isButtonDown(dragButton)) dragButton = -1;
            else if (previousMouse != null) {
                int dx = mouse.x - previousMouse.x, dy = mouse.y - previousMouse.y;
                if (dragButton == 0) {
                    yaw += dx * 0.7f;
                    pitch = Math.max(-85, Math.min(85, pitch + dy * 0.7f));
                } else {
                    Vector3f direction = new Vector3f(renderer.getEyePos()).sub(center)
                        .normalize();
                    Vector3f right = new Vector3f(direction).cross(0, 1, 0)
                        .normalize();
                    Vector3f up = new Vector3f(right).cross(direction)
                        .normalize();
                    center.add(right.mul(dx * distance / 150f))
                        .add(up.mul(dy * distance / 150f));
                }
            }
        }
        previousMouse = mouse;
        renderer.setCameraLookAt(center, distance, Math.toRadians(yaw), Math.toRadians(pitch));
        world.setRenderLayer(layer < 0 ? -1 : (int) world.getMinPos().y + layer);
        try {
            renderer.render(left, top + SCENE_Y, WIDTH, SCENE_HEIGHT, mouse.x, mouse.y);
        } finally {
            world.setRenderLayer(-1);
        }
    }

    boolean click(int x, int y, int button) {
        if (x < 0 || x >= WIDTH) return false;
        if (y >= SCENE_Y && y < SCENE_Y + SCENE_HEIGHT && (button == 0 || button == 1)) {
            dragButton = button;
            return true;
        }
        if (button != 0 || y < BUTTON_Y || y >= BUTTON_Y + 14) return false;
        if (x < 22) yaw -= 90;
        else if (x >= 24 && x < 46) yaw += 90;
        else if (x >= 48 && x < 70) layer = layer < 0 ? layers - 1 : Math.floorMod(layer - 1, layers);
        else if (x >= 72 && x < 94) layer = (layer + 1) % layers;
        else if (x >= 96 && x < 138) layer = -1;
        else if (x >= 142 && x < 164) reset();
        else return false;
        if (layer < 0) renderer.setRenderAllBlocks();
        else renderer.setRenderYLayer(layer);
        return true;
    }

    void zoom(int scroll) {
        distance = Math.max(2, Math.min(defaultDistance * 4, distance * (scroll > 0 ? 0.9f : 1.1f)));
    }

    String layerText() {
        return layer < 0 ? translate("all") : (layer + 1) + "/" + layers;
    }

    MovingObjectPosition hit() {
        return renderer.getLastTraceResult();
    }
}
