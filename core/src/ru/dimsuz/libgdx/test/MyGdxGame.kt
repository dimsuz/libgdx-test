package ru.dimsuz.libgdx.test

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
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
  private var tmpVector3 = Vector3()
  private lateinit var debugShapes: ShapeRenderer

  private var mapWidthTiles: Int = 0
  private var mapHeightTiles: Int = 0
  private var tileWidth: Int = 0
  private var tileHeight: Int = 0

  private var defaultZoom = 1f

  override fun create() {
    batch = SpriteBatch()
    font = BitmapFont()
    debugShapes = ShapeRenderer()
    debugShapes.color = Color.RED

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
    mapViewport = ExtendViewport(
      baseLayer.width.toFloat() * baseLayer.tileWidth,
      baseLayer.height.toFloat() * baseLayer.tileHeight,
      camera
    )
  }

  inner class MouseWheelScrollDetector : InputAdapter() {
    override fun scrolled(amountX: Float, amountY: Float): Boolean {
      camera.zoom = MathUtils.clamp(
        camera.zoom + amountY * MOUSE_WHEEL_ZOOM_FACTOR,
        defaultZoom - (defaultZoom * MAX_ZOOM_FACTOR - defaultZoom),
        defaultZoom
      )
      camera.update()
      makeCameraPositionInBoundsAfterZoom()
      return false
    }
  }

  override fun render() {
    ScreenUtils.clear(0.537f, 0.537f, 0.537f, 1f)
    tiledMapRenderer.setView(camera)
    tiledMapRenderer.render()
    if (debugToolsVisible) {
      batch.begin()
      font.draw(batch, "Pan: xmin=${panMinX}, xmax=${panMaxX})", 16f, 16f + font.lineHeight * 4)
      font.draw(batch, "Pan: ymin=${panMinY}, ymax=${panMaxY})", 16f, 16f + font.lineHeight * 3)
      font.draw(batch, "Camera: (${camera.position.x}, ${camera.position.y})", 16f, 16f + font.lineHeight * 2)
      font.draw(batch, String.format("Zoom: %.3f", camera.zoom), 16f, 16f + font.lineHeight)
      batch.end()

      debugShapes.projectionMatrix = camera.combined
      debugShapes.begin(ShapeRenderer.ShapeType.Line)
      debugShapes.line(0f, panMinY, mapViewport.worldWidth, panMinY)
      debugShapes.end()
      debugShapes.begin(ShapeRenderer.ShapeType.Line)
      debugShapes.line(0f, panMaxY, mapViewport.worldWidth, panMaxY)
      debugShapes.end()
    }
  }

  override fun dispose() {
    tiledMap.dispose()
    batch.dispose()
    font.dispose()
    debugShapes.dispose()
  }

  private var isFirstResize = true
  private var panMinX = 0f
  private var panMaxX = 0f
  private var panMinY = 0f
  private var panMaxY = 0f

  override fun resize(width: Int, height: Int) {
    mapViewport.update(width, height, false)
    camera.position.set(mapViewport.worldWidth / 2f, 0f, 0f)

    if (isFirstResize) {
      panMinX = mapViewport.worldWidth / 2f - (ACTIVE_MAP_SIZE_TILES * tileWidth) / 2f
      panMaxX = mapViewport.worldWidth / 2f + (ACTIVE_MAP_SIZE_TILES * tileWidth) / 2f
      panMinY = -(ACTIVE_MAP_SIZE_TILES * tileHeight) / 2f
      panMaxY = (ACTIVE_MAP_SIZE_TILES * tileHeight) / 2f
      defaultZoom = ACTIVE_MAP_SIZE_TILES / mapWidthTiles.toFloat()
      camera.zoom = defaultZoom
    }

    camera.update()

    isFirstResize = false
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
    tmpVector3.set(camera.position)
    tmpVector3.add(-deltaX, deltaY, 0f)
    // limit panning only if both limit boundaries are outside of the camera's current view, otherwise
    // we'd end up trying to clamp to the boundaries which are already *within* view range, this would require
    // changing zoom
    if (isHorizontalPanRestricted()) {
      val halfViewportWidth = camera.viewportWidth * camera.zoom * 0.5f
      tmpVector3.x = MathUtils.clamp(tmpVector3.x, panMinX + halfViewportWidth, panMaxX - halfViewportWidth)
    }
    if (isVerticalPanRestricted()) {
      val halfViewportHeight = camera.viewportHeight * camera.zoom * 0.5f
      tmpVector3.y = MathUtils.clamp(tmpVector3.y, panMinY + halfViewportHeight, panMaxY - halfViewportHeight)
    }
    camera.position.set(tmpVector3)
    camera.update()
    return false
  }

  private fun isHorizontalPanRestricted(): Boolean {
    return panMaxX - panMinX >= camera.viewportWidth * camera.zoom
  }
  private fun isVerticalPanRestricted(): Boolean {
    return panMaxY - panMinY >= camera.viewportHeight * camera.zoom
  }

  override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
    return false
  }

  override fun zoom(initialDistance: Float, distance: Float): Boolean {
    // TODO @dz: make zoom device independent, and instead of having PINCH_ZOOM_FACTOR, translate pixel distance
    //  directly into zoom increments in the camera coordinate system
    camera.zoom = MathUtils.clamp(
      camera.zoom + PINCH_ZOOM_FACTOR * (initialDistance - distance) / tileWidth,
      defaultZoom - (defaultZoom * MAX_ZOOM_FACTOR - defaultZoom),
      defaultZoom
    )
    camera.update()
    makeCameraPositionInBoundsAfterZoom()
    return false
  }

  private fun makeCameraPositionInBoundsAfterZoom() {
    // ensure pan restrictions are applied (might be broken during zoom out)
    if (isHorizontalPanRestricted()) {
      val halfViewportWidth = camera.viewportWidth * camera.zoom * 0.5f
      if (camera.position.x + halfViewportWidth > panMaxX) {
        camera.position.x = panMaxX - halfViewportWidth
      } else if (camera.position.x - halfViewportWidth < panMinX) {
        camera.position.x = panMinX + halfViewportWidth
      }
    }
    if (isVerticalPanRestricted()) {
      val halfViewportHeight = camera.viewportHeight * camera.zoom * 0.5f
      if (camera.position.y - halfViewportHeight < panMinY) {
        camera.position.y = panMinY + halfViewportHeight
      } else if (camera.position.y + halfViewportHeight > panMaxY) {
        camera.position.y = panMaxY - halfViewportHeight
      }
    }
    camera.update()
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
private const val PINCH_ZOOM_FACTOR = 0.01f

/**
 * A size of the "active map" area in tiles
 */
private const val ACTIVE_MAP_SIZE_TILES = 10

private const val MAX_ZOOM_FACTOR = 1.5f
