import javax.swing.*;
import java.awt.*;

public class Action {

	private int y = 250;
	private int floor_size=50;
	private int stopping_floor =-1;
	private boolean go_up = false;
	private boolean go_down = false;
	private boolean stop_next_floor = false;
	private boolean emergency_stop = false;
	private boolean stopped = false;
	private Instructions ins;
	private static JTextArea textArea = new JTextArea(6,20);

	Action(Instructions ins){
		this.ins=ins;
		ins.set_Actionner(this);
	}

	Instructions get_Instructions(){
		return ins;
	}

	void print_instructions(){
		output_text("        [INSTRUCTION] Séquence d'étage actuelle :\n",true);
		for(Instructions.Command i:ins.get_instructions()){
			output_text("( "+ i.floor +") ",false);
		}
		output_text("",true);
	}

	JTextArea get_text_area(){
		return textArea;
	}

	static void output_text(String text,boolean escape){
		if(escape)
			textArea.append("\n"+text);
		else
			textArea.append(text);
	}

	boolean can_open_doors(){
		if(stopped) {
			return true;
		}
		return false;
	}

	public void doors_closed() {

		ins.closed_doors(true);
	}

	public void doors_openeded() {
		ins.closed_doors(false);
	}

	void go_upstair(){
		go_up=true;
		go_down=false;
		emergency_stop=false;
		stop_next_floor=false;
		stopped=false;
		output_text("    [MOTEUR] L'ascenseur va en haut !",true);
	}

	void go_downstair(){
		go_up=false;
		go_down=true;
		emergency_stop=false;
		stop_next_floor=false;
		stopped=false;
		output_text("    [MOTEUR] L'ascenseur va en bas !",true);

	}

	void next_floor(){
		emergency_stop=false;
		stop_next_floor=true;
		output_text("    [MOTEUR] L'ascenseur s'arrétera au prochain étage !",true);
	}
	void stop_all(){
		if(emergency_stop){
			emergency_stop=false;
			stopped=false;
			ins.emergency_start();
			output_text("    [MOTEUR] Arrêt d'urgence annulé !", true);
		}else {
			go_up = false;
			go_down = false;
			emergency_stop = true;
			stop_next_floor = false;
			stopped = true;
			ins.emergency_stop();
			output_text("    [MOTEUR] Arrêt d'urgence !", true);
		}
	}
	private void stop(){
		go_up=false;
		go_down=false;
		emergency_stop=false;
		stop_next_floor=false;
		stopped=true;
		Action.output_text("    [MOTEUR] Arrêt",true);
	}

	boolean is_moving(){
		return go_up||go_down;
	}

	Point moveElevator() {
		if(emergency_stop){
			go_up=false;
			go_down=false;
		}
		if(!stopped) {
			if (go_up) {
				//On ne monte plus si on arrive au dernier étage
				if(y>0) {
					y -= 2;
					ins.limits_waived();
				} else {
					go_up = false;
					y = 0;
					Action.output_text("    [MOTEUR] Sommet atteint",true);
					ins.upper_limits_reached();
				}
			}
			if (go_down) {
				//On ne descend plus si on arrive au RDC
				if(y<floor_size*5) {
					y += 2;
					ins.limits_waived();
				}else {
					go_down = false;
					y=floor_size*5;
					Action.output_text("    [MOTEUR] Sol atteint",true);
					ins.lowest_limits_reached();
				}
			}
		}
		return new Point(0,y);
	}

	int tmp=-2;

	void detected_floor(int f) {
		//si un étage a été détecté, on vérifie qu'il n'y ait pas eu une demande d'arret au prochain étage

		ins.update_floor_level(f);

		if(stop_next_floor){
			if(tmp==-2) {
				tmp=0;
			}
		}

		if(tmp==0) {
			stop();
			tmp=-2;
		}
	}

}