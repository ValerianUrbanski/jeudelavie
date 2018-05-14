package fr.autre.jeudelavie.ihm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Frame extends Application{
	private int size = 500;
	private int scale = 10;
	private Cells[][] lstPane = new Cells[size][size];
	private List<Cells> lstCells = new ArrayList<Cells>();
	private boolean running = true;
	private boolean paused = false;
	private long start;
	int frameCnt;
	private double gameSpeedSecs = 0.01667;
	private Loop loop;
	private int targetFrame = 60;

	private Label targetFrameLabel = new Label();
	private Label actualFrameLabel = new Label();
	private Label currentStep = new Label();

	private int translatedX=0;
	private int translatedY=0;

	private boolean step = false;
	private boolean moveTo = false;

	private int stepNumber = 0;
	private int moveToNumber = 0;
	private int lifeRule =3;
	@Override
	public void start(Stage stage) throws Exception {
		stage = new Stage();
		start = System.currentTimeMillis();
		BorderPane pane = new BorderPane();
		Scene scene = new Scene(pane,700,700, Color.BLACK);
		Canvas canvas = new Canvas((size*scale)+size, (size*scale)+size);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		canvas.setOnMousePressed(e->{
				int x = (int) e.getX();
				int y = (int) e.getY();
				x = x-translatedX;
				y=y-translatedY;
				for(Cells inLstCells : lstCells){
					if(x-inLstCells.getCoordX() >= 0 && x-inLstCells.getCoordX()<= scale && y-inLstCells.getCoordY()>=0 && y-inLstCells.getCoordY()<=scale){
						if(inLstCells.isAlive()){
							gc.setFill(Color.BLACK);
							gc.fillRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
							inLstCells.setAlive(false);
						}
						else{
							gc.setFill(Color.WHITE);
							gc.fillRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
							inLstCells.setAlive(true);
						}
					}
				}
		});
		Pane panel = new Pane();

		Player player = new Player(0,0);
		panel.setTranslateX(player.getX());
		panel.setTranslateY(player.getY());
		panel.getChildren().add(canvas);
		pane.setCenter(panel);
		VBox box = new VBox();
		TextField field = new TextField();
		field.setDisable(true);
		TextField life = new TextField();
		life.setDisable(true);
		life.setText(String.valueOf(lifeRule));
		Button stepBtn = new Button("Step");
		Button resetStep = new Button("Reset Step");
		Button moveToBtn = new Button("Move to Step");
		life.setOnKeyReleased(e->{
			if(life.getText().length()>1){
				e.consume();
				life.clear();
			}
			else{
				try{
					lifeRule = Integer.valueOf(life.getText());
				}
				catch(Exception e1){
					e1.printStackTrace();
				}
			}
		});
		resetStep.setOnAction(e->{
			stepNumber = 0;
			currentStep.setText("Step Number : "+stepNumber);
			currentStep.setTextFill(Color.WHITE);
		});
		stepBtn.setOnAction(e->{
			step = true;
		});
		moveToBtn.setOnAction(e->{
			try{
				moveToNumber=Integer.valueOf(field.getText());
				moveTo = true;
				field.setText("");
				stepNumber = 0;
			}
			catch(Exception e1){
				e1.printStackTrace();
			}
		});
		Slider slider = new Slider();
		slider.setDisable(true);
		box.setOnMouseEntered(e->{
			slider.setDisable(false);
			field.setDisable(false);
			life.setDisable(false);
		});
		box.setOnMouseExited(e->{
			slider.setDisable(true);
			field.setDisable(true);
			life.setDisable(true);
		});
		slider.setOnMouseEntered(e->{
			slider.setDisable(false);
		});
		slider.setOnMouseExited(e->{
			slider.setDisable(true);
		});
		slider.setMax(60);
		slider.setValue(targetFrame);
		slider.setOnMouseReleased(e->{
			if(Math.round(slider.getValue()) == 0){
				slider.setValue(1);
			}
			slider.setValue(Math.round(slider.getValue()));
			targetFrameLabel.setText("Target Frame : "+String.valueOf(slider.getValue()));
			setTargetFrame((int) slider.getValue());
			gameSpeedSecs = 1/slider.getValue();
			loop.setUpdate((long)(gameSpeedSecs * 1000000000L));
		});
		box.getChildren().addAll(targetFrameLabel,actualFrameLabel,slider,stepBtn,currentStep,resetStep,field,moveToBtn,life);
		pane.setLeft(box);
		pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		targetFrameLabel.setText("Target Frame : "+String.valueOf(getTargetFrame()));
		targetFrameLabel.setTextFill(Color.WHITE);
		for(int i =0;i<size;i++){
			for(int y=0;y<size;y++){
				Cells cell = new Cells((i*scale)+i,(y*scale)+y);
				lstPane[i][y] = cell;
				lstCells.add(cell);
			}
		}
		loop = new Loop((long)(gameSpeedSecs * 1000000000L), elapsed->{
			List<Cells> alive = new ArrayList<Cells>();
			List<Cells> toRez = new ArrayList<Cells>();
			if(stepNumber == moveToNumber){
				moveTo = false;
				currentStep.setText("Step Number : "+stepNumber);
				currentStep.setTextFill(Color.WHITE);
			}
			if(!paused || step || moveTo){
				if(step){
					step = false;
				}
				currentStep.setText("Step Number : "+stepNumber);
				currentStep.setTextFill(Color.WHITE);
				stepNumber++;
				for(int i=0;i<size;i++){
					for(int y=0;y<size;y++){
						int nbAlive = 0;
						int difx =0;
						if(i>0){
							if(i<size-1){
								difx=1;
							}
							else{
								difx=0;
							}
						}
						else{
							difx=0;
						}
						int dify=0;
						if(y>0){
							if(y<size-1){
								dify=1;
							}
							else{
								dify=0;
							}
						}
						else{
							dify=0;
						}
						if(lstPane[i-difx][y-dify].isAlive()){
							nbAlive++;
						}
						if(lstPane[i][y-dify].isAlive()){
							nbAlive++;
						}
						if(lstPane[i+difx][y-dify].isAlive()){
							nbAlive++;
						}
						if(lstPane[i-difx][y].isAlive()){
							nbAlive++;
						}
						if(lstPane[i+difx][y].isAlive()){
							nbAlive++;
						}
						if(lstPane[i-difx][y+dify].isAlive()){
							nbAlive++;
						}
						if(lstPane[i][y+dify].isAlive()){
							nbAlive++;
						}
						if(lstPane[i+difx][y+dify].isAlive()){
							nbAlive++;
						}
						if(nbAlive>lifeRule || nbAlive<(lifeRule-1)){
							alive.add(lstPane[i][y]);
						}
						else if(nbAlive==lifeRule){
							toRez.add(lstPane[i][y]);
						}
					}
				}
				for(Cells inAlive : alive){
					if(inAlive.isAlive()){
						inAlive.setAlive(false);
						gc.setFill(Color.BLACK);
						gc.fillRect(inAlive.getCoordX(), inAlive.getCoordY(), scale, scale);
					}
				}
				alive.clear();
				for(Cells inToRez : toRez){
					if(!inToRez.isAlive()){
						inToRez.setAlive(true);
						gc.setFill(Color.WHITE);
						gc.fillRect(inToRez.getCoordX(), inToRez.getCoordY(), scale, scale);
					}
				}
				toRez.clear();
			}
			long sec = System.currentTimeMillis();
			if((sec - start) >= 1000 ){
				actualFrameLabel.setText("Actual Frame : "+String.valueOf(frameCnt));
				actualFrameLabel.setTextFill(Color.WHITE);
				frameCnt = 0;
				setStart(System.currentTimeMillis());
			}
			else{
				frameCnt++;
				if(frameCnt<getTargetFrame() && gameSpeedSecs>0){
					gameSpeedSecs -= 0.00001;
				}
				else{
					gameSpeedSecs += 0.00001;
				}
				loop.setUpdate((long)(gameSpeedSecs * 1000000000L));
			}
		});
		scene.setOnKeyReleased(e->{
			if(e.getCode().equals(KeyCode.ENTER)){
				if(paused){
					paused = false;
				}
				else{
					paused = true;
				}
			}
			else if(e.getCode().equals(KeyCode.R)){
				for(int i=0;i<size;i++){
					for(int y=0;y<size;y++){
						if(lstPane[i][y].isAlive()){
							lstPane[i][y].setAlive(false);
							gc.setFill(Color.BLACK);
							gc.fillRect(lstPane[i][y].getCoordX(), lstPane[i][y].getCoordY(), scale, scale);
						}
					}
				}
			}
			else if(e.getCode().equals(KeyCode.A)){
				for(int i=0;i<size;i++){
					for(int y=0;y<size;y++){
						if(!lstPane[i][y].isAlive()){
							lstPane[i][y].setAlive(true);
							gc.setFill(Color.WHITE);
							gc.fillRect(lstPane[i][y].getCoordX(), lstPane[i][y].getCoordY(), scale, scale);
						}
					}
				}
			}
			else if(e.getCode().equals(KeyCode.S)){
				for(int i=0;i<size;i++){
					for(int y=0;y<size;y++){
						int rdm = 0 + (int)(Math.random() * ((10 - 0) + 1));
						if(rdm == 10){
							lstPane[i][y].setAlive(true);
							gc.setFill(Color.WHITE);
							gc.fillRect(lstPane[i][y].getCoordX(), lstPane[i][y].getCoordY(), scale, scale);
						}
						else{
							gc.setFill(Color.BLACK);
							gc.fillRect(lstPane[i][y].getCoordX(), lstPane[i][y].getCoordY(), scale, scale);
						}
					}
				}
			}
		});
		scene.setOnKeyPressed(e->{
			switch(e.getCode()){
			case LEFT:{
				translate(-100, 0, gc);
				break;
			}
			case RIGHT:{
				translate(100, 0, gc);
				break;
			}
			case UP:{
				translate(0, -100, gc);
				break;
			}
			case DOWN:{
				translate(0, 100, gc);
				break;
			}
			default:{
				break;
			}
			}
		});
		scene.setOnScroll(e->{
			boolean wasPaused = false;
			if(isPaused()){
				wasPaused = true;
			}
			setPaused(true);
			for(Cells inLstCells : lstCells){
				gc.clearRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
			}
			if(e.getDeltaY()>0){
				scale++;
			}
			else{
				scale--;
			}
			for(int i=0;i<size;i++){
				for(int y=0;y<size;y++){
					Cells cell = lstPane[i][y];
					cell.setCoordX((i*scale)+i);
					cell.setCoordY((y*scale)+y);
				}
			}
			for(Cells inLstCells : lstCells){
				if(inLstCells.isAlive()){
					gc.setFill(Color.WHITE);
					gc.fillRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
				}
				else{
					gc.setFill(Color.BLACK);
					gc.fillRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
				}
			}
			if(!wasPaused){
				setPaused(false);
			}
		});
		stage.setScene(scene);
		stage.setOnCloseRequest(e->{
			running = false;
			Platform.exit();
		});
		stage.show();
		loop.start();
	}
	public void translate(double x, double y, GraphicsContext gc){
		boolean wasPaused = false;
		if(isPaused()){
			wasPaused = true;
		}
		setPaused(true);
		for(Cells inLstCells : lstCells){
			gc.clearRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
		}
		gc.translate(x, y);
		translatedX = translatedX + (int)x;
		translatedY = translatedY +(int)y;
		for(Cells inLstCells : lstCells){
			if(inLstCells.isAlive()){
				gc.setFill(Color.WHITE);
				gc.fillRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
			}
			else{
				gc.setFill(Color.BLACK);
				gc.fillRect(inLstCells.getCoordX(), inLstCells.getCoordY(), scale, scale);
			}
		}
		if(!wasPaused){
			setPaused(false);
		}
	}
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Cells[][] getLstPane() {
		return lstPane;
	}

	public void setLstPane(Cells[][] lstPane) {
		this.lstPane = lstPane;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}

	public int getTargetFrame() {
		return targetFrame;
	}

	public void setTargetFrame(int targetFrame) {
		this.targetFrame = targetFrame;
	}
}
class Cells{
	private boolean alive;
	private int CoordX;
	private int CoordY;
	public Cells(){
		this.alive = true;
	}
	public Cells(int x, int y){
		this.CoordX = x;
		this.CoordY = y;
		this.alive = true;
	}
	public boolean isAlive() {
		return alive;
	}
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	public int getCoordX() {
		return CoordX;
	}
	public void setCoordX(int coordX) {
		CoordX = coordX;
	}
	public int getCoordY() {
		return CoordY;
	}
	public void setCoordY(int coordY) {
		CoordY = coordY;
	}
}
class Loop extends AnimationTimer{

	private long lastTime = IDEALFRAMERATENS;
	private Consumer<Long> doEveryUpdate;
	private long updateGraphicsEvery;

	public Loop(long updateEveryNS, Consumer<Long> f){
		this.updateGraphicsEvery = updateEveryNS;
		this.doEveryUpdate = f;
	}

	@Override
	public void handle(long currentTime) {

		long elapsedTime = currentTime - lastTime;
		if(elapsedTime < updateGraphicsEvery){
			return;
		}
		else{
			lastTime = currentTime;
			doEveryUpdate.accept(elapsedTime);
		}
	}
	public final static long NANOSPERSECOND = 1000000000;
	public final static long IDEALFRAMERATENS = (long)(1 / 60.0 * NANOSPERSECOND);
	public void setUpdate(long update){
		this.updateGraphicsEvery = update;
	}

}
class Player{
	private double x;
	private double y;
	public Player(){

	}
	public Player(double x,double y){
		this.x = x;
		this.y = y;
	}
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}
}
