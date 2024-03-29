package Terrains;

import Models.BasicModel;
import RenderEngine.Loader;
import Textures.ModelTexture;
import Tools.MathLibrary;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Terrain {
	private static final float SIZE = 800;
	private static final float MAX_HEIGHT = 80;
	private static final float MAX_PIXEL_COLOR = 256 * 256 * 256;

	private float x;
	private float z;
	private BasicModel model;
	private ModelTexture texture;
	private float[][] heights;

	public Terrain(int gridX, int gridZ, ModelTexture texture, String heightMap) {
		this.texture = texture;
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		this.model = generateTerrain(heightMap);
	}

	public float getHeightOfTerrain(float worldX, float worldZ) {
		float terrainX = worldX - this.x;
		float terrainZ = worldZ - this.z;
		float gridSquareSize = SIZE / ((float) heights.length - 1);
		int gridX = (int) Math.floor(terrainX / gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
		if (gridX < 0 || gridX >= heights.length - 1 || gridZ < 0 || gridZ >= heights.length - 1) return 999;
		float x = (terrainX % gridSquareSize) / gridSquareSize;
		float z = (terrainZ % gridSquareSize) / gridSquareSize;
		float height;
		if (x <= 1 - z) {
			height = MathLibrary
					.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
							heights[gridX + 1][gridZ], 0), new Vector3f(0,
							heights[gridX][gridZ + 1], 1), new Vector2f(x, z));
		} else {
			height = MathLibrary
					.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
							heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
							heights[gridX][gridZ + 1], 1), new Vector2f(x, z));
		}
		return height;
	}

	private BasicModel generateTerrain(String heightMap) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("res/textures/heightmaps/" + heightMap + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int VERTEX_COUNT = image.getHeight();
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];

		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer = 0;
		for(int i=0;i<VERTEX_COUNT;i++){
			for(int j=0;j<VERTEX_COUNT;j++){
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * SIZE;
				float height = getHeight(j, i, image);
				heights[j][i] = height;
				vertices[vertexPointer*3+1] = height;
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * SIZE;
				Vector3f normal = calculateNormal(j, i, image);
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<VERTEX_COUNT-1;gx++){
				int topLeft = (gz*VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return Loader.loadToVAO(vertices, indices, normals, textureCoords);
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public BasicModel getModel() {
		return model;
	}

	public ModelTexture getTexture() {
		return texture;
	}

	private Vector3f calculateNormal(int x, int y, BufferedImage image) {
		float heightL = getHeight(x-1, y, image);
		float heightR = getHeight(x+1, y, image);
		float heightD = getHeight(x, y-1, image);
		float heightU = getHeight(x, y+1, image);
		Vector3f normal = new Vector3f(heightL-heightR, 2f, heightD-heightU);
		normal.normalize();
		return normal;
	}

	private float getHeight(int x, int y, BufferedImage image) {
		if (x < 0 || x >= image.getHeight() || y < 0 || y >= image.getHeight()) return 0;
		float height = image.getRGB(x, y);
		height += MAX_PIXEL_COLOR / 2;
		height /= MAX_PIXEL_COLOR / 2;
		height *= MAX_HEIGHT;
		return height;
	}
}
