package ru.dimsuz.libgdx.test

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils

class MyGdxGame : ApplicationAdapter(), GestureDetector.GestureListener {
  private lateinit var camera: OrthographicCamera
  private lateinit var tiledMap: TiledMap
  private lateinit var tiledMapRenderer: TiledMapRenderer
  private var mapTop: Float = 0f
  private lateinit var bgLayer: TiledMapTileLayer
  private lateinit var kiosksLayer: MapLayer

  private var kiosksActive: BooleanArray = BooleanArray(0)
  private var kiosksAnimateActive: BooleanArray = BooleanArray(0)
  private var kiosksOpacity: FloatArray = FloatArray(0)
  private var kiosksIds: IntArray = IntArray(0)

  override fun create() {
    camera = OrthographicCamera()
    camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    tiledMap = TmxMapLoader().load("ready.tmx")
    tiledMapRenderer = OrthogonalTiledMapRendererWithObjects(tiledMap)
    bgLayer = tiledMap.layers.get(0) as TiledMapTileLayer
    kiosksLayer = tiledMap.layers.get(1)
    kiosksActive = BooleanArray(kiosksLayer.objects.count)
    kiosksAnimateActive = BooleanArray(kiosksLayer.objects.count)
    kiosksOpacity = FloatArray(kiosksLayer.objects.count) { 0.2f }
    kiosksIds = IntArray(kiosksLayer.objects.count) { kiosksLayer.objects.get(it).properties.get("id") as Int }
    mapTop = bgLayer.height * bgLayer.tileHeight.toFloat()
    camera.position.y = mapTop - camera.viewportHeight / 2
    camera.update()
    Gdx.input.inputProcessor = GestureDetector(this)
  }

  override fun render() {
    ScreenUtils.clear(1f, 0f, 0f, 1f)
    camera.update()
    tiledMapRenderer.setView(camera)

    for (i in kiosksAnimateActive.indices) {
      if (kiosksAnimateActive[i]) {
        val durationSecs = 0.3f
        kiosksOpacity[i] = (kiosksOpacity[i] + Gdx.graphics.deltaTime * 0.8f / durationSecs)
        if (kiosksOpacity[i] >= 1f) {
          kiosksOpacity[i] = 1f
          kiosksActive[i] = true
          kiosksAnimateActive[i] = false
        }
      }
    }

    tiledMapRenderer.render()
  }

  override fun dispose() {
    tiledMap.dispose()
  }

  override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
    return false
  }

  override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
    for ((index, obj) in kiosksLayer.objects.withIndex()) {
      val tapCoord = camera.unproject(Vector3(x, y, 0f))
      val o = obj as TiledMapTileMapObject
      if (tapCoord.x >= o.x && tapCoord.x < o.x + o.tile.textureRegion.regionWidth &&
        tapCoord.y >= o.y && tapCoord.y < o.y + o.tile.textureRegion.regionHeight) {
        kiosksActive[index] = true
        kiosksAnimateActive[index] = true
      }
    }
    return false
  }

  override fun longPress(x: Float, y: Float): Boolean {
    return false
  }

  override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
    return false
  }

  override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
    camera.translate(-deltaX, deltaY)
    if (camera.position.x < camera.viewportWidth / 2) {
      camera.position.x = camera.viewportWidth / 2
    }
    if (camera.position.y > mapTop - camera.viewportHeight / 2) {
      camera.position.y = mapTop - camera.viewportHeight / 2
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

  private inner class OrthogonalTiledMapRendererWithObjects(map: TiledMap) : OrthogonalTiledMapRenderer(map) {

    override fun renderObjects(layer: MapLayer) {
      for ((index, obj) in layer.objects.withIndex()) {
        if (obj is TiledMapTileMapObject) {
          val tile = obj.tile
          val color = batch.color.cpy()
          batch.setColor(color.r, color.g, color.b, kiosksOpacity[index])
          batch.draw(tile.textureRegion, obj.x, obj.y)
          batch.color = color
        }
      }
    }
  }
}

