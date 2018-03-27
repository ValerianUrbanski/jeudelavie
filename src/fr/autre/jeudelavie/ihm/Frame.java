package fr.autre.jeudelavie.ihm;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Frame extends Application{
	private int size = 100;
	private CustomPane[][] lstPane = new CustomPane[size][size];
	private boolean running = true;
	private boolean paused = false;

	@Override
	public void start(Stage stage) throws Exception {
		stage = new Stage();

		GridPane gridPane = new GridPane();
		gridPane.setVgap(1);
		gridPane.setHgap(1);
		gridPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		for(int i =0;i<size;i++){
			for(int y=0;y<size;y++){
				CustomPane pane = new CustomPane();
				pane.setMinSize(10, 10);
				pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
				pane.setBorder(new Border(new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
				pane.setOnMouseClicked(e->{
					if(pane.isAlive()){
						pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
						pane.setAlive(false);
					}
					else{
						pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
						pane.setAlive(true);
					}
				});
				lstPane[i][y] = pane;
				gridPane.add(pane, i, y);
			}
		}
		new Thread(){
			public void run() {
				while(true && running){
					List<CustomPane> alive = new ArrayList<CustomPane>();
					List<CustomPane> toRez = new ArrayList<CustomPane>();
					System.out.println("doshit");
					if(!paused){
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
								if(nbAlive>3 || nbAlive<2){
									alive.add(lstPane[i][y]);
								}
								else if(nbAlive==3){
									toRez.add(lstPane[i][y]);
								}
							}
						}
						for(CustomPane inAlive : alive){
							if(inAlive.isAlive()){
								inAlive.setAlive(false);
								inAlive.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
							}
						}
						alive.clear();
						for(CustomPane inToRez : toRez){
							if(!inToRez.isAlive()){
								inToRez.setAlive(true);
								inToRez.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
							}
						}
						toRez.clear();
					}
				}
			}
		}.start();
		Scene scene = new Scene(gridPane,700,700);
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
							lstPane[i][y].setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
						}
					}
				}
			}
			else if(e.getCode().equals(KeyCode.A)){
				for(int i=0;i<size;i++){
					for(int y=0;y<size;y++){
						if(!lstPane[i][y].isAlive()){
							lstPane[i][y].setAlive(true);
							lstPane[i][y].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
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
							lstPane[i][y].setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
						}
					}
				}
			}
		});
		stage.setScene(scene);
		stage.setOnCloseRequest(e->{
			running = false;
			Platform.exit();
		});
		stage.show();

	}
}
class CustomPane extends Pane{
	private boolean alive;
	public CustomPane(){
		super();
		this.alive = true;
	}
	public boolean isAlive() {
		return alive;
	}
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
