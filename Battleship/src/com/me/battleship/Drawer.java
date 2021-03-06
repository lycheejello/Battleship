package com.me.battleship;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.GL10;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Drawer {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ArrayList<Sprite> sprites;
    private Board board;
    private Properties props = new Properties();
    private int screenWidth, screenHeight, tileSize;
    private Texture tileTexture, selectedTileTexture;
    private TextureRegion[] frigate, cruiser, battleship;
    private Button rotateRegion;

    public Drawer(Board board, Button rotateRegion) {
        try {
            props.load(Gdx.files.internal("data/config.properties").read());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(10);
        }
        this.board = board;
        this.rotateRegion = rotateRegion;
        camera = new OrthographicCamera(Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        // Set it to an orthographic projection with "y down"
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        tileSize = (int) board.dimensions.x / board.getSize();
        camera.setToOrtho(true, screenWidth, screenHeight);
        camera.update();

        // Create a full screen sprite renderer and use the above camera
        batch = new SpriteBatch(screenHeight, screenHeight);
        batch.setProjectionMatrix(camera.combined);
        sprites = new ArrayList<Sprite>();

        loadTextures();
    }

    private Sprite createSprite(String texturePath, int width, int height) {
        Texture texture = new Texture(Gdx.files.internal(texturePath));
        TextureRegion region = new TextureRegion(texture, width, height);
        return new Sprite(region);

    }

    public void drawSetup() {

        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        /*
         * batch.draw(board, Globals.GridLocation1.x, Globals.GridLocation1.y,
         * Globals.GridSize1.x, Globals.GridSize1.y); for (int i = 0; i <
         * player1.ships.dimensions(); i++) batch.draw(player1.ships.get(i).tex,
         * player1.ships.get(i).x, player1.ships.get(i).y,
         * player1.ships.get(i).width, player1.ships.get(i).height);
         */
        //for (Sprite sprite : sprites) { sprite.draw(batch); }

        // draw the setup grid
        int tileSize = getTileSize();
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                batch.draw(tileTexture, x * tileSize + board.topLeft.x, y * tileSize + board.topLeft.y,
                        tileSize, tileSize);
            }
        }

        //draw rotate zone
        batch.draw(tileTexture, rotateRegion.topLeft.x, rotateRegion.topLeft.y, rotateRegion.dimensions.x, rotateRegion.dimensions.y);

        //draw the ships
        for (Ship ship : board.getShips()) {
            TextureRegion shipTexture;
            int orientationOrdinal = ship.getOrientation().getOrdinal();
            switch (ship.getShipClass()) {
                //TODO
                case PATROL:
                    shipTexture = frigate[orientationOrdinal];
                    break;
                case FRIGATE:
                case DESTROYER:
                    shipTexture = cruiser[orientationOrdinal];
                    break;
                case BATTLESHIP:
                case CARRIER:
                default:
                    shipTexture = battleship[orientationOrdinal];
                    break;
            }
            batch.draw(shipTexture, ship.topLeft.x, ship.topLeft.y, ship.dimensions.x, ship.dimensions.y);

            //draw selected
            for (int j = 0; j < ship.getActiveSquares().length; j++) {
                if (ship.getActiveSquares()[j]) {
                    batch.draw(selectedTileTexture,
                            ship.getOnSquares()[j].x * tileSize + board.topLeft.x,
                            ship.getOnSquares()[j].y * tileSize + board.topLeft.y,
                            tileSize, tileSize);
                }
            }

        }
        batch.end();
    }

    public void dispose() {
        batch.dispose();
    }

    public int getTileSize() {
        return tileSize;
    }

    private void loadTextures() {

        // background
        /*
        Sprite bg = createSprite(props.getProperty("texture.background"), 1280, 720);
        bg.setPosition(0, 0);
        sprites.add(bg);
        */

        // create border
        //Sprite p1GridBorder = createSprite("data/board/large_teal_border.png", 512, 512);
        //p1GridBorder.setSize(screenHeight * .75f, screenHeight * .75f);
        // bgSprite.setOrigin(bgSprite.getWidth() / 2, bgSprite.getHeight() /
        // 2);
        // bgSprite.setPosition(-bgSprite.getWidth()/2,
        // -bgSprite.getHeight()/2);
        //p1GridBorder.setPosition(25, 100);
        //sprites.add(p1GridBorder);
        // Sprite p2GridBorder = new Sprite(p1GridBorder);
        // p2GridBorder.setPosition(screenWidth - p1GridBorder.getX() -
        // p2GridBorder.getWidth(), p1GridBorder.getY());
        // sprites.add(p2GridBorder);

        Texture shipTextures;
        shipTextures = new Texture(Gdx.files.internal(props.getProperty("texture.ships")));
        frigate = textureFlipper(new TextureRegion(shipTextures, 0, 0, 40, 40),
                new TextureRegion(shipTextures, 40, 0, 40, 40));
        cruiser = textureFlipper(new TextureRegion(shipTextures, 0, 60, 80, 40),
                new TextureRegion(shipTextures, 0, 100, 40, 80));
        battleship = textureFlipper(new TextureRegion(shipTextures, 0, 200,
                160, 40), new TextureRegion(shipTextures, 0, 240, 40, 160));
        //TODO

        tileTexture = new Texture(Gdx.files.internal(props.getProperty("texture.tile.empty")));
        selectedTileTexture = new Texture(Gdx.files.internal(props.getProperty("texture.tile.selected")));
    }

    private static TextureRegion[] textureFlipper(TextureRegion... textures) {
        for (int i = 0; i < textures.length; i++)
            textures[i].flip(false, true);
        return textures;
    }

    public void centerShipOnGrid(Ship s) {
        int temp_adjust;
        float temp_adjust_half, x, y;
        temp_adjust = (s.getShipClass().getLength()) / 2;
        float half_tile = .5f * tileSize;
        temp_adjust_half = (s.getShipClass().getLength()) % 2 * half_tile;
        switch (s.getOrientation()) {
            case HORIZONTAL:
                x = board.topLeft.x + s.getOnSquares()[temp_adjust].x * tileSize + temp_adjust_half;
                y = board.topLeft.y + s.getOnSquares()[temp_adjust].y * tileSize + half_tile;
                break;
            case VERTICAL:
            default:
                x = board.topLeft.x + s.getOnSquares()[temp_adjust].x * tileSize + half_tile;
                y = board.topLeft.y + s.getOnSquares()[temp_adjust].y * tileSize + temp_adjust_half;
                break;

        }
        s.move(x, y);
    }

    public void highlightSquares(int x, int y, Ship s) {
        int length = s.getShipClass().getLength();
        int j = -(length - 1) / 2;
        int temp_x, temp_y;
        switch (s.getOrientation()) {
            case HORIZONTAL:
                temp_x = (int) ((x - tileSize / 2 - board.topLeft.x) / tileSize);
                temp_y = (int) ((y - board.topLeft.y) / tileSize);
                if (temp_y >= 0 && temp_y < board.getSize()) {
                    for (int i = 0; i < s.getOnSquares().length; i++) {

                        if (temp_x + j >= 0 && temp_x + j < board.getSize()) {
                            s.getOnSquares()[i].x = temp_x + j;
                            s.getOnSquares()[i].y = temp_y;
                            s.getActiveSquares()[i] = true;
                        } else
                            s.getActiveSquares()[i] = false;
                        j++;
                    }
                } else
                    s.deselectSquares();
                break;
            case VERTICAL:
            default:
                temp_x = (int) (x - board.topLeft.x) / tileSize;
                temp_y = (int) (y - tileSize / 2 - board.topLeft.y) / tileSize;
                if (temp_x >= 0 && temp_x < board.getSize()) {
                    for (int i = 0; i < s.getOnSquares().length; i++) {
                        if (temp_y + j >= 0 && temp_y + j < board.getSize()) {
                            s.getOnSquares()[i].x = temp_x;
                            s.getOnSquares()[i].y = temp_y + j;
                            s.getActiveSquares()[i] = true;
                        } else
                            s.getActiveSquares()[i] = false;
                        j++;
                    }
                } else
                    s.deselectSquares();
                break;
        }
    }
}
