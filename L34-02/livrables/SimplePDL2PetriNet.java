package simplepdl.manip;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import simplepdl.Process;
import simplepdl.WorkDefinition;
import simplepdl.WorkSequence;
import simplepdl.WorkSequenceType;
import simplepdl.SimplepdlPackage;
import simplepdl.Ressources;
import simplepdl.Control;
import petrinet.*;

public class SimplePDL2PetriNet {
	
	static Map<String, Place> Ready = new HashMap<String, Place>();
	static Map<String, Place> Started = new HashMap<String, Place>();
	static Map<String, Place> Finished = new HashMap<String, Place>();
	static Map<String, Place> Running = new HashMap<String, Place>();
	static Map<String, Transition> Start = new HashMap<String, Transition>();
	static Map<String, Transition> Finish = new HashMap<String, Transition>();
	

	public static void main(String[] args) {
		
		// Chargement du package SimplePDL afin de l'enregistrer dans le registre d'Eclipse.
		SimplepdlPackage packageInstanceSimplePDL = SimplepdlPackage.eINSTANCE;
		// Chargement du package PetriNet afin de l'enregistrer dans le registre d'Eclipse.
		PetrinetPackage packageInstancePetriNet = PetrinetPackage.eINSTANCE;
		
		// Enregistrer l'extension ".xmi" comme devant Ãªtre ouverte Ã 
		// l'aide d'un objet "XMIResourceFactoryImpl"
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		
		// CrÃ©er un objet resourceSetImpl qui contiendra une ressource EMF (notre modÃ¨le)
		ResourceSet resSet = new ResourceSetImpl();
		
		// Créer le modèle de réseau de Pétri
		URI petriURI = URI.createURI("transformationtopetri.xmi");
		Resource petri = resSet.createResource(petriURI);
		
		// Créer ressource
		ResourceSet resSetModel = new ResourceSetImpl();

		// Charger la ressource (notre modÃ¨le)
		URI modelURI = URI.createURI("mysimplepdl.xmi");
		Resource resource = resSetModel.getResource(modelURI, true);
	    
		// RÃ©cupÃ©rer le premier Ã©lÃ©ment du modÃ¨le (Ã©lÃ©ment racine)
		Process process = (Process) resource.getContents().get(0);
		
		// La fabrique pour fabriquer les Ã©lÃ©ments de PetriNet
	    PetrinetFactory myFactory = PetrinetFactory.eINSTANCE;
		PetriNet myPetriNet = myFactory.createPetriNet();
		myPetriNet.setName(process.getName());
		petri.getContents().add(myPetriNet);
		
		for (Object o : process.getProcessElements()) {
			
			Transformations(myPetriNet, myFactory, o);
		}
		
		// Sauver la ressource
	    try {
	    	petri.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	static public void Transformations(PetriNet myPetriNet, PetrinetFactory myFactory, Object o) {
		
		if(o instanceof WorkDefinition) {
			
			// Créer les places
			Place PlaceReady = myFactory.createPlace();
			Place PlaceStarted = myFactory.createPlace();
			Place PlaceFinished = myFactory.createPlace();
			Place PlaceRunning = myFactory.createPlace();

			// Créer les transitions
			Transition TransitionStarted = myFactory.createTransition();
			Transition TransitionFinished = myFactory.createTransition();
			
			// Créer les arcs
			Arc ArcPReady2TStarted = myFactory.createArc();
			Arc ArcPRunning2TFinished = myFactory.createArc();
			Arc ArcTStarted2PStarted = myFactory.createArc();
			Arc ArcTStarted2PRunning = myFactory.createArc();
			Arc ArcTFinished2PFinished = myFactory.createArc();

			// Node.name
			PlaceReady.setName(((WorkDefinition)o).getName() + "Ready");
			PlaceRunning.setName(((WorkDefinition)o).getName() + "Running");
			PlaceStarted.setName(((WorkDefinition)o).getName() + "Started");
			PlaceFinished.setName(((WorkDefinition)o).getName() + "Finished");
			TransitionStarted.setName(((WorkDefinition)o).getName() + "TStarted");
			TransitionFinished.setName(((WorkDefinition)o).getName() + "TFinished");
			

			// Placetokens
			PlaceReady.setTokens(1);
			PlaceStarted.setTokens(0);
			PlaceFinished.setTokens(0);
			PlaceRunning.setTokens(0);
			
			// Construction des arcs 
			ArcPReady2TStarted.setNodesource(PlaceReady);
			ArcPReady2TStarted.setNodedestination(TransitionStarted);
			ArcPRunning2TFinished.setNodesource(PlaceRunning);
			ArcPRunning2TFinished.setNodedestination(TransitionFinished);
			ArcTStarted2PStarted.setNodesource(TransitionStarted);
			ArcTStarted2PStarted.setNodedestination(PlaceStarted);
			ArcTStarted2PRunning.setNodesource(TransitionStarted);
			ArcTStarted2PRunning.setNodedestination(PlaceRunning);
			ArcTFinished2PFinished.setNodesource(TransitionFinished);
			ArcTFinished2PFinished.setNodedestination(PlaceFinished);


			// Arc.arc
			ArcPReady2TStarted.setArc(TypeArc.NORMAL);
			ArcPRunning2TFinished.setArc(TypeArc.NORMAL);
			ArcTStarted2PStarted.setArc(TypeArc.NORMAL);
			ArcTStarted2PRunning.setArc(TypeArc.NORMAL);
			ArcTFinished2PFinished.setArc(TypeArc.NORMAL);

			// Poids de l'arc
			ArcPReady2TStarted.setWeigth(1);
			ArcPRunning2TFinished.setWeigth(1);
			ArcTStarted2PStarted.setWeigth(1);
			ArcTStarted2PRunning.setWeigth(1);
			ArcTFinished2PFinished.setWeigth(1);

			// Ajout des Places à mypetri
			myPetriNet.getPetrinetelement().add(PlaceReady);
			myPetriNet.getPetrinetelement().add(PlaceStarted);
			myPetriNet.getPetrinetelement().add(PlaceRunning);
			myPetriNet.getPetrinetelement().add(PlaceFinished);

			// Ajout des Transitions à mypetri
			myPetriNet.getPetrinetelement().add(TransitionStarted);
			myPetriNet.getPetrinetelement().add(TransitionFinished);

			// Ajout des Arcs à mypetri
			myPetriNet.getPetrinetelement().add(ArcPReady2TStarted);
			myPetriNet.getPetrinetelement().add(ArcPRunning2TFinished);
			myPetriNet.getPetrinetelement().add(ArcTStarted2PStarted);
			myPetriNet.getPetrinetelement().add(ArcTStarted2PRunning);
			myPetriNet.getPetrinetelement().add(ArcTFinished2PFinished);

			//Mise à jour des maps
			Ready.put(((WorkDefinition)o).getName(), PlaceReady);
			Started.put(((WorkDefinition)o).getName(), PlaceStarted);
			Running.put(((WorkDefinition)o).getName(), PlaceRunning);
			Finished.put(((WorkDefinition)o).getName(), PlaceFinished);
			Start.put(((WorkDefinition)o).getName(), TransitionStarted);
			Finish.put(((WorkDefinition)o).getName(), TransitionFinished);
		}
		else if (o instanceof WorkSequence) {
			Arc myArc = myFactory.createArc();
			if(((WorkSequence)o).getLinkType() == WorkSequenceType.START_TO_START) {
				myArc.setNodesource(Started.get(((WorkSequence)o).getPredecessor().getName()));
				myArc.setNodedestination(Start.get(((WorkSequence)o).getSuccessor().getName()));
			}
			else if(((WorkSequence)o).getLinkType() == WorkSequenceType.START_TO_FINISH) {
				myArc.setNodesource(Started.get(((WorkSequence)o).getPredecessor().getName()));
				myArc.setNodedestination(Finish.get(((WorkSequence)o).getSuccessor().getName()));
			}
			else if(((WorkSequence)o).getLinkType() == WorkSequenceType.FINISH_TO_START) {
				myArc.setNodesource(Finished.get(((WorkSequence)o).getPredecessor().getName()));
				myArc.setNodedestination(Start.get(((WorkSequence)o).getSuccessor().getName()));
			}
			else if(((WorkSequence)o).getLinkType() == WorkSequenceType.FINISH_TO_FINISH) {
				myArc.setNodesource(Finished.get(((WorkSequence)o).getPredecessor().getName()));
				myArc.setNodedestination(Finish.get(((WorkSequence)o).getSuccessor().getName()));
			}
			myArc.setArc(TypeArc.READ_ARC);
			myArc.setWeigth(1);
			myPetriNet.getPetrinetelement().add(myArc);
		}
		
		else if (o instanceof Ressources) {
			// Création de la Place Ressource
			Place PlaceRessource = myFactory.createPlace();

				// Paramétrage des Places
			// Nommer la ressource
			PlaceRessource.setName("Ressource_" + ((Ressources)o).getName());
			// Initialisation du nombre de jeton
			PlaceRessource.setTokens(((Ressources)o).getQuantity());
			
			// Ajout de la Place à mypetri
			myPetriNet.getPetrinetelement().add(PlaceRessource);
			
			// Liaison Ressource-WorkDefinition
			for (Control c : ((Ressources)o).getControl()) {
				// Ajout de l'arc entre la PlaceRessource et 
				// la transition start correspondante
				Arc arcControl = myFactory.createArc();
				arcControl.setNodesource(PlaceRessource);
				System.out.println(c.getWorkdefinition());
				arcControl.setNodedestination(Start.get(c.getWorkdefinition()));
				arcControl.setWeigth(c.getOccurences());
				myPetriNet.getPetrinetelement().add(arcControl);
				
				// Ajout de l'arc entre la PlaceRessource et 
				// la transition finish correspondante
				Arc arcRetour = myFactory.createArc();
				arcRetour.setNodesource(Finish.get(c.getWorkdefinition()));
				arcRetour.setNodedestination(PlaceRessource);
				arcRetour.setWeigth(c.getOccurences());
				myPetriNet.getPetrinetelement().add(arcRetour);
			}
			
		}

	}

}
