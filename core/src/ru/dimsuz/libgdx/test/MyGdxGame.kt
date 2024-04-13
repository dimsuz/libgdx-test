package ru.dimsuz.libgdx.test

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport

class MyGdxGame : ApplicationAdapter(), GestureDetector.GestureListener {
  private lateinit var camera: OrthographicCamera
  private lateinit var tiledMap: TiledMap
  private lateinit var tiledMapRenderer: IsometricTiledMapRenderer
  private lateinit var mapViewport: Viewport
  private lateinit var batch: SpriteBatch
  private lateinit var font: BitmapFont
  private var debugToolsVisible = true

  private var mapWidthTiles: Int = 0
  private var mapHeightTiles: Int = 0
  private var tileWidth: Int = 0
  private var tileHeight: Int = 0

  private var defaultZoom = 1f

  override fun create() {
    batch = SpriteBatch()
    font = BitmapFont()

    tiledMap = TmxMapLoader().load("aplCityProto1.tmx")
    tiledMapRenderer = IsometricTiledMapRenderer(tiledMap)
    val multiplexer = InputMultiplexer()
    multiplexer.addProcessor(GestureDetector(this))
    multiplexer.addProcessor(MouseWheelScrollDetector())
    Gdx.input.inputProcessor = multiplexer

    camera = OrthographicCamera()
    val baseLayer = tiledMapRenderer.map.layers[0] as TiledMapTileLayer
    mapWidthTiles = baseLayer.width
    mapHeightTiles = baseLayer.height
    tileWidth = baseLayer.tileWidth
    tileHeight = baseLayer.tileHeight
    mapViewport = ExtendViewport(baseLayer.width.toFloat() * baseLayer.tileWidth, baseLayer.height.toFloat() * baseLayer.tileHeight, camera)
  }

  inner class MouseWheelScrollDetector : InputAdapter() {
    override fun scrolled(amountX: Float, amountY: Float): Boolean {
      camera.zoom = MathUtils.clamp(camera.zoom + amountY * MOUSE_WHEEL_ZOOM_FACTOR, defaultZoom * (MAX_ZOOM_FACTOR - 1), defaultZoom)
      camera.update()
      return false
    }
  }

  override fun render() {
    ScreenUtils.clear(0.537f, 0.537f, 0.537f, 1f)
    tiledMapRenderer.setView(camera)
    tiledMapRenderer.render()
    if (debugToolsVisible) {
      batch.begin()
      font.draw(batch, "Tile size: ${tileWidth}x${tileHeight}", 16f, 16f + font.lineHeight * 3)
      font.draw(batch, "MapSize: ${mapWidthTiles}x${mapHeightTiles}", 16f, 16f + font.lineHeight * 2)
      font.draw(batch, String.format("Zoom: %.3f", camera.zoom), 16f, 16f + font.lineHeight)
      batch.end()
    }
  }

  override fun dispose() {
    tiledMap.dispose()
    batch.dispose()
    font.dispose()
  }

  override fun resize(width: Int, height: Int) {
    mapViewport.update(width, height, false)
    camera.position.set(mapViewport.worldWidth/2f, 0f, 0f)
    if (defaultZoom == 1f) {
      defaultZoom = ACTIVE_MAP_SIZE_TILES / mapWidthTiles.toFloat()
      camera.zoom = defaultZoom
    }
    camera.update()
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
    mapViewport.camera.translate(-deltaX, deltaY, 0f)
    mapViewport.camera.update()
    return false
  }

  override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
    return false
  }

  override fun zoom(initialDistance: Float, distance: Float): Boolean {
    camera.zoom -= 0.1f
    camera.update()
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

private const val MOUSE_WHEEL_ZOOM_FACTOR = 0.1f

/**
 * A size of the "active map" area in tiles
 */
private const val ACTIVE_MAP_SIZE_TILES = 10

private const val MAX_ZOOM_FACTOR = 1.5f
