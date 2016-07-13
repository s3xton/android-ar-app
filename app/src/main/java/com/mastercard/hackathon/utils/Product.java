package com.mastercard.hackathon.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.vuforia.Mesh;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Conor on 13/07/2016.
 *
 *  A class to represent a product in the store. Models/textures can either be loaded dynamically
 *  by the object, or given to the object via a setters.
 */
public class Product {
    private String name;
    private String description;
    transient private MeshObject model;
    transient private Texture[] textures;
    private double price;
    private boolean isModelLoaded;
    private boolean isTextureLoaded;

    private String objectFilename;
    private String[] textureFilenames;

    public Product(String name, String description, double price, String objectFilename,
                   String[] textureFilenames) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.objectFilename = objectFilename;
        this.textureFilenames = textureFilenames;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public MeshObject getModel() {
        return this.model;
    }

    public double getPrice() {
        return this.price;
    }

    public boolean isModelLoaded() {
        return this.isModelLoaded;
    }

    public boolean isTextureLoaded() {
        return this.isTextureLoaded;
    }

    public void setModel(MeshObject model){
        this.model = model;
        this.isModelLoaded = true;
    }

    public void setTextures(Texture[] textures) {
        this.textures = textures;
        this.isTextureLoaded = true;
    }

    private void loadModel(Context context) throws IOException {
        if(objectFilename != null) {
            SampleApplication3DModel model = new SampleApplication3DModel();
            model.loadModel(context.getAssets(), objectFilename);
            this.model = model;
            this.isModelLoaded = true;
        }
    }

    private void loadTextures(Context context) {
        if(textureFilenames != null){
            for(int i = 0; i < textureFilenames.length; i++) {
                textures[i] = Texture.loadTextureFromApk(textureFilenames[i], context.getAssets());
            }
            this.isTextureLoaded = true;
        }
    }



}
