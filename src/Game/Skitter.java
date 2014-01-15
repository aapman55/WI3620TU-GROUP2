package Game;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import org.newdawn.slick.openal.OggData;
import org.newdawn.slick.openal.OggDecoder;

import Game.Monster;
import Game.Player;

public class Skitter {
	/** Maximum data buffers we will need. */
	public static final int NUM_BUFFERS = 256;

	/** Maximum emissions we will need. */
	public static final int NUM_SOURCES = 256;

	/** Index of game background sound */
	public static final int backgroundgame = 0;

	/** Index of menu background sound */
	public static final int backgroundmenu = 1;
	
	/** Index of hurt sound */
	public static final int hurt = 2;
	
	/** Index of hurt sound */
	public static final int explosion = 240;

	/** Index of skitter sound */
	public static final int skitter = 4;

	/** Index of gun 2 sound */
	// public static final int GUN2 = 3;

	/** Buffers hold sound data. */
	IntBuffer buffer = BufferUtils.createIntBuffer(NUM_BUFFERS);

	/** Sources are points emitting sound. */
	IntBuffer source = BufferUtils.createIntBuffer(NUM_BUFFERS);

	/** Position of the source sound. */
	FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3 * NUM_BUFFERS);

	/** Velocity of the source sound. */
	FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3 * NUM_BUFFERS);

	/** Position of the listener. */
	FloatBuffer listenerPos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();

	/** Velocity of the listener. */
	FloatBuffer listenerVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();

	/**
	 * Orientation of the listener. (first 3 elements are "at", second 3 are
	 * "up")
	 */
	FloatBuffer listenerOri = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }).rewind();

	/**
	 * boolean LoadALData()
	 * 
	 * This function will load our sample data from the disk using the Alut
	 * utility and send the data into OpenAL as a buffer. A source is then also
	 * created to play that buffer.
	 */
	int loadALData() {

		// Overload buffers
		AL10.alGenBuffers(buffer);

		if (AL10.alGetError() != AL10.AL_NO_ERROR)
			return AL10.AL_FALSE;

		// Loads the wave files for the sound effects
		java.io.FileInputStream fin = null;
		
		// Skitter sound
		try {
			fin = new java.io.FileInputStream("res/Sound/skitter.wav");
		} catch (java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			return AL10.AL_FALSE;
		}
		WaveData waveFile = WaveData.create(new BufferedInputStream(fin));
		AL10.alBufferData(buffer.get(skitter), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		
		// Hurt sound
		try {
			fin = new java.io.FileInputStream("res/Sound/hurt.wav");
		} catch (java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			return AL10.AL_FALSE;
		}
		waveFile = WaveData.create(new BufferedInputStream(fin));
		AL10.alBufferData(buffer.get(hurt), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();

		// Explosion sound
		try {
			fin = new java.io.FileInputStream("res/Sound/explosion.wav");
		} catch (java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			return AL10.AL_FALSE;
		}
		waveFile = WaveData.create(new BufferedInputStream(fin));
		AL10.alBufferData(buffer.get(explosion), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		
		
		// // Load background oggs

		try {
			fin = new java.io.FileInputStream("res/Sound/background_x2.ogg");
		} catch (java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			return AL10.AL_FALSE;
		}
		OggDecoder decoder = new OggDecoder();
		OggData ogg;
		try {
			ogg = decoder.getData(fin);
			AL10.alBufferData(buffer.get(backgroundgame), ogg.channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, ogg.data, ogg.rate);
			System.out.println("Error?: " + (AL10.alGetError() != AL10.AL_NO_ERROR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fin.close();
		} catch (java.io.IOException ex) {
		}

		try {
			fin = new java.io.FileInputStream("res/Sound/background_menu.ogg");
		} catch (java.io.FileNotFoundException ex) {
			ex.printStackTrace();
			return AL10.AL_FALSE;
		}
		decoder = new OggDecoder();
		try {
			ogg = decoder.getData(fin);
			AL10.alBufferData(buffer.get(backgroundmenu), ogg.channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, ogg.data, ogg.rate);
			System.out.println("Error?: " + (AL10.alGetError() != AL10.AL_NO_ERROR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fin.close();
		} catch (java.io.IOException ex) {
		}

		//
		AL10.alGenSources(source);
		
		//binding hurt soundeffect
		AL10.alSourcei(source.get(hurt), AL10.AL_BUFFER, buffer.get(hurt));
		AL10.alSourcef(source.get(hurt), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(source.get(hurt), AL10.AL_GAIN, 0.5f);
		AL10.alSource(source.get(hurt), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(hurt * 3));
		AL10.alSource(source.get(hurt), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(hurt * 3));
		AL10.alSourcei(source.get(hurt), AL10.AL_LOOPING, AL10.AL_FALSE);
		
		//binding background music
		AL10.alSourcei(source.get(backgroundgame), AL10.AL_BUFFER, buffer.get(backgroundgame));
		AL10.alSourcef(source.get(backgroundgame), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(source.get(backgroundgame), AL10.AL_GAIN, 0.5f);
		AL10.alSource(source.get(backgroundgame), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(backgroundgame * 3));
		AL10.alSource(source.get(backgroundgame), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(backgroundgame * 3));
		AL10.alSourcei(source.get(backgroundgame), AL10.AL_LOOPING, AL10.AL_TRUE);

		AL10.alSourcei(source.get(backgroundmenu), AL10.AL_BUFFER, buffer.get(backgroundmenu));
		AL10.alSourcef(source.get(backgroundmenu), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(source.get(backgroundmenu), AL10.AL_GAIN, 1.0f);
		AL10.alSource(source.get(backgroundmenu), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(backgroundmenu * 3));
		AL10.alSource(source.get(backgroundmenu), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(backgroundmenu * 3));
		AL10.alSourcei(source.get(backgroundmenu), AL10.AL_LOOPING, AL10.AL_TRUE);
		
		// Do another error check and return.
				if (AL10.alGetError() == AL10.AL_NO_ERROR)
					return AL10.AL_TRUE;

				return AL10.AL_FALSE;
	}

	public void bindMonsters(ArrayList<Monster> monsterlijst) {
		// Bind buffers into audio sources.
		

		for (Monster mon : monsterlijst) {

			AL10.alSourcei(source.get(mon.getID()), AL10.AL_BUFFER, buffer.get(skitter));
			AL10.alSourcef(source.get(mon.getID()), AL10.AL_PITCH, 1.0f);
			AL10.alSourcef(source.get(mon.getID()), AL10.AL_GAIN, 1.0f);
			AL10.alSource(source.get(mon.getID()), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(mon.getID() * 3));
			AL10.alSource(source.get(mon.getID()), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(mon.getID() * 3));
			AL10.alSourcei(source.get(mon.getID()), AL10.AL_LOOPING, AL10.AL_FALSE);

		}

		
	}

	/**
	 * void setListenerValues()
	 * 
	 * We already defined certain values for the Listener, but we need to tell
	 * OpenAL to use that data. This function does just that.
	 */
	void setListenerValues() {
		AL10.alListener(AL10.AL_POSITION, listenerPos);
		AL10.alListener(AL10.AL_VELOCITY, listenerVel);
		AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
	}

	/**
	 * void killALData()
	 * 
	 * We have allocated memory for our buffers and sources which needs to be
	 * returned to the system. This function frees that memory.
	 */
	void killALData() {
		AL10.alDeleteSources(source);
		AL10.alDeleteBuffers(buffer);
	}

	public void execute() {
		// Initialize OpenAL and clear the error bit.
		// try{
		// AL.create();
		// } catch (LWJGLException le) {
		// le.printStackTrace();
		// return;
		// }
		// AL10.alGetError();

		// Load the wav data.
		if (loadALData() == AL10.AL_FALSE) {
			System.out.println("Error loading data.");
			return;
		}

		setListenerValues();

		
		
	}

	public void updatemonsters(ArrayList<Monster> monsterlijst) {
		for (Monster mon : monsterlijst) {
			sourcePos.position(mon.getID() * 3);
			sourcePos.put((float) mon.getLocationX());
			sourcePos.put((float) mon.getLocationY());
			sourcePos.put((float) mon.getLocationZ());
			AL10.alSource(source.get(mon.getID()), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(mon.getID() * 3));
		}
		sourcePos.rewind();
	}

	public void playMenu() {
		AL10.alSourcePlay(source.get(backgroundmenu));
	}

	public void stopMenu() {
		AL10.alSourceStop(source.get(backgroundmenu));
	}

	public void playGame() {
		AL10.alSourcePlay(source.get(backgroundgame));
	}

	public void stopGame() {
		AL10.alSourceStop(source.get(backgroundgame));
	}
	
	public void playHurt(){
		System.out.println("skitter-hurt!");
		AL10.alSourcePlay(source.get(hurt));
	}

	public void play(ArrayList<Monster> monsterlijst, Player player) {
		listenerPos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[] { (float) player.getLocationX(), (float) player.getLocationY(), (float) player.getLocationZ() }).rewind();
		listenerOri = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[] { (float) player.lookat().getX(), (float) player.lookat().getY(), (float) player.lookat().getZ(), 0.0f, 1.0f, 0.0f }).rewind();
		setListenerValues();
		int playing;
		for (Monster mon : monsterlijst) {
//			System.out.println("playcheck");
			playing = AL10.alGetSourcei(source.get(mon.getID()), AL10.AL_SOURCE_STATE);
			if (playing != AL10.AL_PLAYING) {
				AL10.alSourcePlay(source.get(mon.getID()));
			}

		}
	}
	
	public void playExplosion(double x, double y, double z){
		int sourceid=explosion;
		int playing = AL10.alGetSourcei(source.get(sourceid), AL10.AL_SOURCE_STATE);
		while(playing == AL10.AL_PLAYING){
			sourceid++;
			playing = AL10.alGetSourcei(source.get(sourceid), AL10.AL_SOURCE_STATE);
		}
		//dan lege source: positie instellen en binden
		
		// positie
		sourcePos.position(sourceid * 3);
		sourcePos.put((float) x);
		sourcePos.put((float) y);
		sourcePos.put((float) z);
		
		//binding explosion soundeffect
		AL10.alSourcei(source.get(sourceid), AL10.AL_BUFFER, buffer.get(explosion));
		AL10.alSourcef(source.get(sourceid), AL10.AL_PITCH, 1.0f);
		AL10.alSourcef(source.get(sourceid), AL10.AL_GAIN, 1.0f);
		AL10.alSource(source.get(sourceid), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(sourceid * 3));
		AL10.alSource(source.get(sourceid), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(sourceid * 3));
		AL10.alSourcei(source.get(sourceid), AL10.AL_LOOPING, AL10.AL_FALSE);
		
		//and play
		AL10.alSourcePlay(source.get(sourceid));
	}

	public void cleanup() {
		AL10.alDeleteSources(source);
		AL10.alDeleteBuffers(buffer);
		// AL.destroy();
	}

	public void deleteSources() {
		AL10.alDeleteSources(source);
	}

}