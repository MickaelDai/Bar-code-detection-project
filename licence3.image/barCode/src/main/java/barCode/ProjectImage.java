package barCode;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, menuPath = "Plugins>Bar code detection>ProjectionImage")
public class ProjectImage<T extends RealType<T>> implements Command {

	@Parameter(type = ItemIO.INPUT, required = true)
	ImgPlus<T> inputImage;

	@Parameter(type = ItemIO.INPUT, required = true)
	boolean horizontal = true; // true = horizontal projection,
	// false = vertical projection

	@Parameter(type = ItemIO.OUTPUT)
	ImgPlus<IntType> projection;

	@Override
	public void run() {

		long[] dims = new long[inputImage.numDimensions()];
		inputImage.dimensions(dims);

		// La taille du resultat est <largeur, 10> (projection horizontale)
		// ou <10, hauteur> (projection verticale)
		long[] projDims = new long[] { horizontal ? dims[0] : 10, horizontal ? 10 : dims[1] };
		projection = ImgPlus.wrap(ArrayImgs.ints(projDims));

		RandomAccess<T> inputImageCursor = inputImage.randomAccess();
		RandomAccess<IntType> projectionCursor = projection.randomAccess();

		long[] posInputImage = new long[inputImage.numDimensions()];
		long[] posProjection = new long[projection.numDimensions()];

		// Completez ce code:
		// 1. Pour Chaque ligne/colonne de l'image d'entree
		// 2. On somme les intensités de toutes les intensités de la colonne/ligne
		// 3. On affecte la somme au pixel(s) de l'image resultat "projection"

		for (int i = 0; i < inputImage.dimension(horizontal ? 0 : 1); i++) {
			posInputImage[horizontal ? 0 : 1] = i;
			posProjection[horizontal ? 0 : 1] = i;
			double sum = 0;
			for (int j = 0; j < inputImage.dimension(horizontal ? 1 : 0); j++) {
				posInputImage[horizontal ? 1 : 0] = j;
				inputImageCursor.setPosition(posInputImage);
				sum += inputImageCursor.get().getRealDouble();
			}

			for (int j = 0; j < projDims[horizontal ? 1 : 0]; j++) {
				posProjection[horizontal ? 1 : 0] = j;
				projectionCursor.setPosition(posProjection);
				projectionCursor.get().set((int) Math.round(sum));
			}
		}
		// Curseur de la projection
		RandomAccess<IntType> ProjectionCursor = projection.randomAccess();
		// On définit la position du curseur
		long[] currentPosition = new long[posProjection.length];
		int decodeurBarre = 0;
		ArrayList<Integer> listBarre = new ArrayList<Integer>();
		// Compteurs de pixel
		int compteurNoir = 0;
		int compteurBlanc = 0;
		// On parcourt les lignes
		for (int i = 0; i <= posInputImage[0]; i++) {
			currentPosition[0] = i;
			ProjectionCursor.setPosition(currentPosition);
			// Si la valeur est égal à 0, il s'agit d'un pixel noir
			if (ProjectionCursor.get().getRealDouble() == 0) {
				// System.out.println("C'est noir");
				compteurNoir++;
				System.out.println("Noir "+compteurNoir);
				if ((compteurBlanc == 4) || (compteurBlanc == 5) || (compteurBlanc ==6)) {
					decodeurBarre = 1;

					listBarre.add(decodeurBarre);
				} else if ((compteurBlanc == 9) || (compteurBlanc == 10)) {
					decodeurBarre = 2;

					listBarre.add(decodeurBarre);
				}
				if ((compteurBlanc == 13) || (compteurBlanc == 14) || (compteurBlanc == 15)) {
					decodeurBarre = 3;

					listBarre.add(decodeurBarre);
				}
				if ((compteurBlanc == 19) || (compteurBlanc == 20)) {
					decodeurBarre = 4;

					listBarre.add(decodeurBarre);
				}
				compteurBlanc = 0;
			}

			// Sinon c'est un pixel noir
			else {
				compteurBlanc++;
				System.out.println("Blanc "+compteurBlanc);
				if ((compteurNoir == 4) || (compteurNoir == 5) || (compteurNoir ==6)){
					decodeurBarre = 1;

					listBarre.add(decodeurBarre);
				} else if ((compteurNoir == 9) || (compteurNoir == 10)) {
					decodeurBarre = 2;

					listBarre.add(decodeurBarre);
				}
				if ((compteurNoir == 13) || (compteurNoir == 14) || (compteurNoir == 15)) {
					decodeurBarre = 3;

					listBarre.add(decodeurBarre);
				}
				if ((compteurNoir == 19) || (compteurNoir == 20)) {
					decodeurBarre = 4;

					listBarre.add(decodeurBarre);
				}
				compteurNoir = 0;
			}

		
		}
		//Il y a "11111" d'inutile

		
		int miniCompteur = 0;
		System.out.println("la liste: ");
		for (int l : listBarre) {
			System.out.print(l);
			miniCompteur++;
			if (miniCompteur == 4){
				System.out.print(" ");
				miniCompteur = 0;
			}

			
		}
		System.out.println("\n Lecture du code barre :");
		
		////lecture du code barre
		System.out.print("||");
		//System.out.print(lectureCodeBarre((listBarre.subList(0, 3))));
		System.out.print(lectureCodeBarre((listBarre.subList(3, 7))));
		System.out.print(lectureCodeBarre((listBarre.subList(7, 11))));
		System.out.print(lectureCodeBarre((listBarre.subList(11, 15))));
		System.out.print(lectureCodeBarre((listBarre.subList(15, 19))));
		System.out.print("||");
		System.out.print(lectureCodeBarre((listBarre.subList(24, 28))));
		System.out.print(lectureCodeBarre((listBarre.subList(28, 32))));
		System.out.print(lectureCodeBarre((listBarre.subList(32, 36))));
		System.out.print(lectureCodeBarre((listBarre.subList(36, 40))));
		System.out.print("||");
				
	}
	


	//méthode pour détecter chaque chiffre
	
	public int lectureCodeBarre(List<Integer> list) {
		int result = -1;
		
		//Création des listes de chiffres
		ArrayList<Integer> l0 = new ArrayList<Integer>();
		ArrayList<Integer> l1 = new ArrayList<Integer>();
		ArrayList<Integer> l2 = new ArrayList<Integer>();
		ArrayList<Integer> l3 = new ArrayList<Integer>();
		ArrayList<Integer> l4 = new ArrayList<Integer>();
		ArrayList<Integer> l5 = new ArrayList<Integer>();
		ArrayList<Integer> l6 = new ArrayList<Integer>();
		ArrayList<Integer> l7 = new ArrayList<Integer>();
		ArrayList<Integer> l8 = new ArrayList<Integer>();
		ArrayList<Integer> l9 = new ArrayList<Integer>();
		
		//Application du tableau dans chaque liste
				//Pour 0
				l0.add(3);
				l0.add(2);
				l0.add(1);
				l0.add(1);
				//Pour 1
				l1.add(2);
				l1.add(2);
				l1.add(2);
				l1.add(1);
				//Pour 2
				l2.add(2);
				l2.add(1);
				l2.add(2);
				l2.add(2);
				//Pour 3
				l3.add(1);
				l3.add(4);
				l3.add(1);
				l3.add(1);
				//Pour 4
				l4.add(1);
				l4.add(1);
				l4.add(3);
				l4.add(2);
				//Pour 5
				l5.add(1);
				l5.add(2);
				l5.add(3);
				l5.add(1);
				//Pour 6
				l6.add(1);
				l6.add(1);
				l6.add(1);
				l6.add(4);
				//Pour 7
				l7.add(1);
				l7.add(3);
				l7.add(1);
				l7.add(2);
				//Pour 8
				l8.add(1);
				l8.add(2);
				l8.add(1);
				l8.add(3);
				//Pour 9
				l9.add(3);
				l9.add(1);
				l9.add(1);
				l9.add(2);	
		
		//On vérifie la bonne écriture
		if (list.equals(l0)) {
			return 0;
		}
		if (list.equals(l1)) {
			return 1;
		}
		if (list.equals(l2)) {
			return 2;
		}
		if (list.equals(l3)) {
			return 3;
		}
		if (list.equals(l4)) {
			return 4;
		}
		if (list.equals(l5)) {
			return 5;
		}
		if (list.equals(l6)) {
			return 6;
		}
		if (list.equals(l7)) {
			return 7;
		}
		if (list.equals(l8)) {
			return 8;
		}
		if (list.equals(l9)) {
			return 9;
		}
		return result;
	}

}
