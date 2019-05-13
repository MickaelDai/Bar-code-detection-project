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

@Plugin(type = Command.class, menuPath = "Plugins>Bar code detection>Projection et lecture")
public class ProjectionEtLecture<T extends RealType<T>> implements Command {

	@Parameter(type = ItemIO.INPUT, required = true)
	ImgPlus<T> inputImage;

	@Parameter(type = ItemIO.INPUT, required = false)
	boolean horizontal = true; // true = horizontal projection,
	// false = vertical projection

	@Parameter(type = ItemIO.INPUT, required = true)
	boolean ean = true; // true = ean 8,
	// false = ean 13

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

		if (ean) {
			// On parcourt les lignes
			for (int i = 0; i <= posInputImage[0]; i++) {
				currentPosition[0] = i;
				ProjectionCursor.setPosition(currentPosition);
				// Si la valeur est égal à 0, il s'agit d'un pixel noir
				if (ProjectionCursor.get().getRealDouble() == 0) {
					// System.out.println("C'est noir");
					compteurNoir++;
					System.out.println("Noir " + compteurNoir);
					if ((compteurBlanc == 4) || (compteurBlanc == 5) || (compteurBlanc == 6)) {
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

				// Sinon c'est un pixel blanc
				else {
					compteurBlanc++;
					System.out.println("Blanc " + compteurBlanc);
					if ((compteurNoir == 4) || (compteurNoir == 5) || (compteurNoir == 6)) {
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
			// Il y a "11111" d'inutile

			int miniCompteur = 0;
			System.out.println("la liste: ");
			for (int l : listBarre) {
				System.out.print(l);
				miniCompteur++;
				if (miniCompteur == 4) {
					System.out.print(" ");
					miniCompteur = 0;
				}

			}
			System.out.println("\n Lecture du code barre ean-8 :");

			//// lecture du code barre
			System.out.print("||");
			// System.out.print(lectureCodeBarre((listBarre.subList(0, 3))));
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

		} else {
			System.out.println("Test Ean 13");
			// On parcourt les lignes
			for (int i = 0; i <= posInputImage[0]; i++) {
				currentPosition[0] = i;
				ProjectionCursor.setPosition(currentPosition);
				// Si la valeur est égal à 0, il s'agit d'un pixel noir
				if (ProjectionCursor.get().getRealDouble() == 0) {
					// System.out.println("C'est noir");
					compteurNoir++;
					System.out.println("Noir " + compteurNoir);
					if ((compteurBlanc == 3) || (compteurBlanc == 2) || (compteurBlanc == 4)) {
						decodeurBarre = 1;

						listBarre.add(decodeurBarre);
					} else if ((compteurBlanc == 5) || (compteurBlanc == 6) || (compteurBlanc == 7)) {
						decodeurBarre = 2;

						listBarre.add(decodeurBarre);
					}
					if ((compteurBlanc == 9) || (compteurBlanc == 10)) {
						decodeurBarre = 3;

						listBarre.add(decodeurBarre);
					}
					if ((compteurBlanc == 12) || (compteurBlanc == 13)) {
						decodeurBarre = 4;

						listBarre.add(decodeurBarre);
					}
					compteurBlanc = 0;
				}

				// Sinon c'est un pixel blanc
				else {
					compteurBlanc++;
					System.out.println("Blanc " + compteurBlanc);
					if ((compteurNoir == 3) || (compteurNoir == 2) || (compteurNoir == 4)) {
						decodeurBarre = 1;

						listBarre.add(decodeurBarre);
					} else if ((compteurNoir == 5) || (compteurNoir == 6) || (compteurNoir == 7)) {
						decodeurBarre = 2;

						listBarre.add(decodeurBarre);
					}
					if ((compteurNoir == 9) || (compteurNoir == 10)) {
						decodeurBarre = 3;

						listBarre.add(decodeurBarre);
					}
					if ((compteurNoir == 12) || (compteurNoir == 13)) {
						decodeurBarre = 4;

						listBarre.add(decodeurBarre);
					}
					compteurNoir = 0;
				}

			}
			// Il y a "11111" d'inutile

			int miniCompteur = 0;
			System.out.println("la liste: ");
			for (int l : listBarre) {
				System.out.print(l);
				miniCompteur++;
				if (miniCompteur == 4) {
					System.out.print(" ");
					miniCompteur = 0;
				}

			}
			System.out.println("\n Lecture du code barre ean-13 :");

			//// lecture du code barre

			System.out.print("||");
			// System.out.print(lectureCodeBarre((listBarre.subList(0, 3))));
			System.out.print(lectureCodeBarre((listBarre.subList(3, 7))));
			System.out.print(lectureCodeBarre((listBarre.subList(7, 11))));
			System.out.print(lectureCodeBarre((listBarre.subList(11, 15))));
			System.out.print(lectureCodeBarre((listBarre.subList(15, 19))));
			System.out.print(lectureCodeBarre((listBarre.subList(19, 23))));
			System.out.print(lectureCodeBarre((listBarre.subList(23, 27))));
			// System.out.print(lectureCodeBarre((listBarre.subList(27, 32))));
			System.out.print("||");
			System.out.print(lectureCodeBarre((listBarre.subList(32, 36))));
			System.out.print(lectureCodeBarre((listBarre.subList(36, 40))));
			System.out.print(lectureCodeBarre((listBarre.subList(40, 44))));
			System.out.print(lectureCodeBarre((listBarre.subList(44, 48))));
			System.out.print(lectureCodeBarre((listBarre.subList(48, 52))));
			System.out.print(lectureCodeBarre((listBarre.subList(52, 56))));
			System.out.print("||");
			System.out.println(detectePremierChiffre((l.subList(0, 6))));

		}

	}

	static ArrayList<String> l = new ArrayList<String>();
	// méthode pour détecter chaque chiffre

	public int lectureCodeBarre(List<Integer> list) {
		// Création des listes de chiffres
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

		// Création des listes de chiffres
		ArrayList<Integer> l0b = new ArrayList<Integer>();
		ArrayList<Integer> l1b = new ArrayList<Integer>();
		ArrayList<Integer> l2b = new ArrayList<Integer>();
		ArrayList<Integer> l3b = new ArrayList<Integer>();
		ArrayList<Integer> l4b = new ArrayList<Integer>();
		ArrayList<Integer> l5b = new ArrayList<Integer>();
		ArrayList<Integer> l6b = new ArrayList<Integer>();
		ArrayList<Integer> l7b = new ArrayList<Integer>();
		ArrayList<Integer> l8b = new ArrayList<Integer>();
		ArrayList<Integer> l9b = new ArrayList<Integer>();

		// Application du tableau dans chaque liste de type A ou C
		// Pour 0
		l0.add(3);
		l0.add(2);
		l0.add(1);
		l0.add(1);
		// Pour 1
		l1.add(2);
		l1.add(2);
		l1.add(2);
		l1.add(1);
		// Pour 2
		l2.add(2);
		l2.add(1);
		l2.add(2);
		l2.add(2);
		// Pour 3
		l3.add(1);
		l3.add(4);
		l3.add(1);
		l3.add(1);
		// Pour 4
		l4.add(1);
		l4.add(1);
		l4.add(3);
		l4.add(2);
		// Pour 5
		l5.add(1);
		l5.add(2);
		l5.add(3);
		l5.add(1);
		// Pour 6
		l6.add(1);
		l6.add(1);
		l6.add(1);
		l6.add(4);
		// Pour 7
		l7.add(1);
		l7.add(3);
		l7.add(1);
		l7.add(2);
		// Pour 8
		l8.add(1);
		l8.add(2);
		l8.add(1);
		l8.add(3);
		// Pour 9
		l9.add(3);
		l9.add(1);
		l9.add(1);
		l9.add(2);

		// Application du tableau dans chaque liste de type B
		// Pour 0
		l0b.add(1);
		l0b.add(1);
		l0b.add(2);
		l0b.add(3);
		// Pour 1
		l1b.add(1);
		l1b.add(2);
		l1b.add(2);
		l1b.add(2);
		// Pour 2
		l2b.add(2);
		l2b.add(2);
		l2b.add(1);
		l2b.add(2);
		// Pour 3
		l3b.add(1);
		l3b.add(1);
		l3b.add(4);
		l3b.add(1);
		// Pour 4
		l4b.add(2);
		l4b.add(3);
		l4b.add(1);
		l4b.add(1);
		// Pour 5
		l5b.add(1);
		l5b.add(3);
		l5b.add(2);
		l5b.add(1);
		// Pour 6
		l6b.add(4);
		l6b.add(1);
		l6b.add(1);
		l6b.add(1);
		// Pour 7
		l7b.add(2);
		l7b.add(1);
		l7b.add(3);
		l7b.add(1);
		// Pour 8
		l8b.add(3);
		l8b.add(1);
		l8b.add(2);
		l8b.add(1);
		// Pour 9
		l9b.add(2);
		l9b.add(1);
		l9b.add(1);
		l9b.add(3);

		// On vérifie la bonne écriture
		if ((list.equals(l0)) || (list.equals(l0b))) {
			if (list.equals(l0)) {
				l.add("a");
			} else if (list.equals(l0b)) {
				l.add("b");
			}
			return 0;
		} else if ((list.equals(l1)) || (list.equals(l1b))) {
			if (list.equals(l1)) {
				l.add("a");
			} else if (list.equals(l1b)) {
				l.add("b");
			}
			return 1;
		} else if ((list.equals(l2)) || (list.equals(l2b))) {
			if (list.equals(l2)) {
				l.add("a");
			} else if (list.equals(l2b)) {
				l.add("b");
			}
			return 2;
		} else if ((list.equals(l3)) || (list.equals(l3b))) {
			if (list.equals(l3)) {
				l.add("a");
			} else if (list.equals(l3b)) {
				l.add("b");
			}
			return 3;
		} else if ((list.equals(l4)) || (list.equals(l4b))) {
			if (list.equals(l4)) {
				l.add("a");
			} else if (list.equals(l4b)) {
				l.add("b");
			}
			return 4;
		} else if ((list.equals(l5)) || (list.equals(l5b))) {
			if (list.equals(l5)) {
				l.add("a");
			} else if (list.equals(l5b)) {
				l.add("b");
			}
			return 5;
		} else if ((list.equals(l6)) || (list.equals(l6b))) {
			if (list.equals(l6)) {
				l.add("a");
			} else if (list.equals(l6b)) {
				l.add("b");
			}
			return 6;
		} else if ((list.equals(l7)) || (list.equals(l7b))) {
			if (list.equals(l7)) {
				l.add("a");
			} else if (list.equals(l7b)) {
				l.add("b");
			}
			return 7;
		} else if ((list.equals(l8)) || (list.equals(l8b))) {
			if (list.equals(l8)) {
				l.add("a");
			} else if (list.equals(l8b)) {
				l.add("b");
			}
			return 8;
		} else if ((list.equals(l9)) || (list.equals(l9b))) {
			if (list.equals(l9)) {
				l.add("a");
			} else if (list.equals(l9b)) {
				l.add("b");
			}
			return 9;
		}
		return -1;
	}

	public int detectePremierChiffre(List<String> clist) {
		// Création des listes de detection de type
		ArrayList<String> lc0 = new ArrayList<String>();
		ArrayList<String> lc1 = new ArrayList<String>();
		ArrayList<String> lc2 = new ArrayList<String>();
		ArrayList<String> lc3 = new ArrayList<String>();
		ArrayList<String> lc4 = new ArrayList<String>();
		ArrayList<String> lc5 = new ArrayList<String>();
		ArrayList<String> lc6 = new ArrayList<String>();
		ArrayList<String> lc7 = new ArrayList<String>();
		ArrayList<String> lc8 = new ArrayList<String>();
		ArrayList<String> lc9 = new ArrayList<String>();

		// Affectation 0
		lc0.add("a");
		lc0.add("a");
		lc0.add("a");
		lc0.add("a");
		lc0.add("a");
		lc0.add("a");
		
		// Affectation 1
		lc1.add("a");
		lc1.add("a");
		lc1.add("b");
		lc1.add("a");
		lc1.add("b");
		lc1.add("b");
		
		// Affectation 2
		lc2.add("a");
		lc2.add("a");
		lc2.add("b");
		lc2.add("b");
		lc2.add("a");
		lc2.add("b");
		
		// Affectation 3
		lc3.add("a");
		lc3.add("a");
		lc3.add("b");
		lc3.add("b");
		lc3.add("b");
		lc3.add("a");
		
		// Affectation 4
		lc4.add("a");
		lc4.add("b");
		lc4.add("a");
		lc4.add("a");
		lc4.add("b");
		lc4.add("b");
		
		// Affectation 5
		lc5.add("a");
		lc5.add("b");
		lc5.add("b");
		lc5.add("a");
		lc5.add("a");
		lc5.add("b");
		
		// Affectation 6
		lc6.add("a");
		lc6.add("b");
		lc6.add("b");
		lc6.add("b");
		lc6.add("a");
		lc6.add("a");
		
		// Affectation 7
		lc7.add("a");
		lc7.add("b");
		lc7.add("a");
		lc7.add("b");
		lc7.add("a");
		lc7.add("b");
		
		// Affectation 8
		lc8.add("a");
		lc8.add("b");
		lc8.add("a");
		lc8.add("b");
		lc8.add("b");
		lc8.add("a");
		
		// Affectation 9
		lc9.add("a");
		lc9.add("b");
		lc9.add("b");
		lc9.add("a");
		lc9.add("b");
		lc9.add("a");

		if (clist.equals(lc0)) {
			return 0;
		} else if (clist.equals(lc1)) {
			return 1;
		} else if (clist.equals(lc2)) {
			return 2;
		} else if (clist.equals(lc3)) {
			return 3;
		} else if (clist.equals(lc4)) {
			return 4;
		} else if (clist.equals(lc5)) {
			return 5;
		} else if (clist.equals(lc6)) {
			return 6;
		} else if (clist.equals(lc7)) {
			return 7;
		} else if (clist.equals(lc8)) {
			return 8;
		} else if (clist.equals(lc9)) {
			return 9;
		}
		return -1;

	}
}
