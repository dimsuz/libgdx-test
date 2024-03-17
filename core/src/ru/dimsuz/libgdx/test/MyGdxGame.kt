package ru.dimsuz.libgdx.test

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.O
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils

class MyGdxGame : ApplicationAdapter(), GestureDetector.GestureListener {
  private lateinit var camera: OrthographicCamera
  private lateinit var tiledMap: TiledMap
  private lateinit var tiledMapRenderer: TiledMapRenderer

  override fun create() {
    camera = OrthographicCamera()
    camera.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    camera.update()
    tiledMap = TmxMapLoader().load("ready.tmx")
    tiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap)
    Gdx.input.inputProcessor = GestureDetector(this)
  }

  override fun render() {
    ScreenUtils.clear(1f, 0f, 0f, 1f)
    camera.update()
    tiledMapRenderer.setView(camera)
    tiledMapRenderer.render()
  }

  override fun dispose() {
  }

  override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
    return false
  }

  override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
    return false
  }

  override fun longPress(x: Float, y: Float): Boolean {
    return false
  }

  override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
    return false
  }

  override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
    camera.translate(-deltaX, -deltaY)
    if (camera.position.x < camera.viewportWidth / 2) {
      camera.position.x = camera.viewportWidth / 2
    }
    if (camera.position.y < camera.viewportHeight / 2) {
      camera.position.y = camera.viewportHeight / 2
    }
    return false
  }

  override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
    return false
  }

  override fun zoom(initialDistance: Float, distance: Float): Boolean {
    return false
  }

  override fun pinch(
    initialPointer1: Vector2?,
    initialPointer2: Vector2?,
    pointer1: Vector2?,
    pointer2: Vector2?
  ): Boolean {
    return false
  }

  override fun pinchStop() {
  }

}
