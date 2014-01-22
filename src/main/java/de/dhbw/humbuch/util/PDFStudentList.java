package de.dhbw.humbuch.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

import de.dhbw.humbuch.model.SubjectHandler;
import de.dhbw.humbuch.model.entity.BorrowedMaterial;
import de.dhbw.humbuch.model.entity.Student;


public final class PDFStudentList extends PDFHandler {

	private Student student;
	private List<BorrowedMaterial> borrowedMaterialList;
	private List<BorrowedMaterial> returnList;
	private List<BorrowedMaterial> lendingList;

	private Set<Builder> builders;

	/**
	 * For each student in the builder objects a PDF is created. This PDF can
	 * contain three different kinds of lists.
	 * 
	 * @param builder
	 *            can contain three kinds of lists. One list of materials the
	 *            student already received, one list of materials the student
	 *            will receive and one list of materials the student has to
	 *            return
	 * 
	 */
	public PDFStudentList(Builder... builder) {
		super();
		this.builders = new LinkedHashSet<>();
		for (Builder b : builder) {
			this.builders.add(b);
		}
	}

	/**
	 * For each student in the builder objects a PDF is created. This PDF can
	 * contain three different kinds of lists.
	 * 
	 * @param builder
	 *            can contain three kinds of lists. One list of materials the
	 *            student already received, one list of materials the student
	 *            will receive and one list of materials the student has to
	 *            return
	 * 
	 */
	public PDFStudentList(Set<Builder> builder) {
		super();
		this.builders = builder;
	}

	protected void insertDocumentParts(Document document) {
		for (Builder builder : builders) {
			if (builder.student != null) {
				this.student = builder.student;
			}
			else {
				continue;
			}
			this.borrowedMaterialList = builder.borrowedMaterialList;
			this.lendingList = builder.lendingList;
			this.returnList = builder.returnList;

			this.addHeading(document);
			this.addStudentInformation(document);
			this.addInformationAboutDocument(document, "Informations-Liste");
			this.addContent(document);
			document.newPage();
			this.resetPageNumber();
		}
	}

	protected void addContent(Document document) {
		if (this.borrowedMaterialList != null && !this.borrowedMaterialList.isEmpty()) {
			PdfPTable table = PDFHandler.createMyStandardTable(1);
			PDFHandler.fillTableWithContentWithoutAlignment(table, false,
					new String[] { "\nDie folgenden Bücher befinden sich im Besitz des Schülers/der Schülerin:" },
					FontFactory.getFont("Helvetica", 10, Font.BOLD));
			try {
				document.add(table);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			}

			table = this.createTableWithRentalInformationHeader();

			for (BorrowedMaterial borrowedMaterial : this.borrowedMaterialList) {
				String[] contentArray = { borrowedMaterial.getTeachingMaterial().getName(),
											"" + borrowedMaterial.getTeachingMaterial().getToGrade(),
											"" };
				PDFHandler.fillTableWithContentWithoutSpace(table, true, contentArray, true, 5f);
			}
			try {
				document.add(table);
				PDFHandler.addEmptyLineToDocument(document, 1);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		if (this.returnList != null && !this.returnList.isEmpty()) {
			PdfPTable table = PDFHandler.createMyStandardTable(1);
			PDFHandler.fillTableWithContentWithoutAlignment(table, false,
					new String[] { "\n Die folgenden Bücher müssen zurückgegeben werden:" },
					FontFactory.getFont("Helvetica", 10, Font.BOLD));
			try {
				document.add(table);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			}

			table = this.createTableWithRentalInformationHeader();

			for (BorrowedMaterial borrowedMaterial : this.returnList) {
				String[] contentArray = { borrowedMaterial.getTeachingMaterial().getName(),
											"" + borrowedMaterial.getTeachingMaterial().getToGrade(),
											"" };

				PDFHandler.fillTableWithContentWithoutSpace(table, true, contentArray, true, 5f);

			}

			try {
				document.add(table);
				this.addRentalDisclosure(document);
				this.addSignatureField(document, "Lehrer");
				PDFHandler.addEmptyLineToDocument(document, 1);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		if (this.lendingList != null && !this.lendingList.isEmpty()) {
			PdfPTable table = PDFHandler.createMyStandardTable(1);
			PDFHandler.fillTableWithContentWithoutAlignment(table, false,
					new String[] { "\n Die folgenden Bücher sollen ausgeliehen werden:" },
					FontFactory.getFont("Helvetica", 10, Font.BOLD));
			try {
				document.add(table);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			}

			table = this.createTableWithRentalInformationHeader();

			for (BorrowedMaterial borrowedMaterial : this.lendingList) {
				String[] contentArray = { borrowedMaterial.getTeachingMaterial().getName(),
											"" + borrowedMaterial.getTeachingMaterial().getToGrade(),
											"" };

				PDFHandler.fillTableWithContentWithoutSpace(table, true, contentArray, true, 5f);
			}

			try {
				document.add(table);
			}
			catch (DocumentException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Inserts information about the student like grade, language, name etc.
	 * 
	 * @param document
	 *            represents the PDF before it is saved
	 */
	private void addStudentInformation(Document document) {
		PdfPTable table = PDFHandler.createMyStandardTable(2, new float[] { 1f, 6f });

		String[] contentArray = { "Schüler: ", this.student.getFirstname() + " " + this.student.getLastname(),
									"Klasse: ", "" + this.student.getGrade().toString(),
									"Sprachen: ", SubjectHandler.getLanguageProfile(this.student.getProfile()),
									"Religion: ", SubjectHandler.getReligionProfile(this.student.getProfile())};
		PDFHandler.fillTableWithContentWithoutSpace(table, false, contentArray, false, 0f);

		try {
			document.add(table);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param document
	 *            represents the PDF before it is saved
	 */
	private void addRentalDisclosure(Document document) {
		PdfPTable table = PDFHandler.createMyStandardTable(1);
		PDFHandler.fillTableWithContent(table, false,
				new String[] { "\nDie oben angeführten Schulbücher hat der Schüler zurückgegeben.\n" +
						"Die ausgeliehenen Bücher wurden auf Vollständigkeit und Beschädigung überprüft. " +
						"Beschädigte oder verlorengegangene Bücher wurden ersetzt.\n" }, false);
		try {
			document.add(table);
		}
		catch (DocumentException e) {
			e.printStackTrace();
		}
	}


	public static class Builder {

		private Student student;
		private List<BorrowedMaterial> borrowedMaterialList;
		private List<BorrowedMaterial> lendingList;
		private List<BorrowedMaterial> returnList;

		/**
		 * A builder object can contain three different kinds of lists. Each
		 * list is optional. The builder can be passed to the constructor of
		 * PDFStudentList and the lists the builder contains will be printed
		 */
		public Builder() {
		}

		/**
		 * 
		 * @param borrowedMaterialList
		 *            list of materials the student already received
		 * @return the builder object that has to be passed to the
		 *         PDFStudentList constructor
		 */
		public Builder borrowedMaterialList(List<BorrowedMaterial> borrowedMaterialList) {
			this.borrowedMaterialList = borrowedMaterialList;
			if (borrowedMaterialList != null) {
				this.student = borrowedMaterialList.get(0).getStudent();
			}
			return this;
		}

		/**
		 * 
		 * @param borrowedMaterialList
		 *            list of materials the student will receive
		 * @return the builder object that has to be passed to the
		 *         PDFStudentList constructor
		 */
		public Builder lendingList(List<BorrowedMaterial> borrowedMaterialList) {
			this.lendingList = borrowedMaterialList;
			if (borrowedMaterialList != null) {
				this.student = borrowedMaterialList.get(0).getStudent();
			}
			return this;
		}

		/**
		 * 
		 * @param borrowedMaterialList
		 *            list of materials the student has to return
		 * @return
		 */
		public Builder returnList(List<BorrowedMaterial> borrowedMaterialList) {
			this.returnList = borrowedMaterialList;
			if (borrowedMaterialList != null) {
				this.student = borrowedMaterialList.get(0).getStudent();
			}
			return this;
		}

		@Deprecated
		public PDFStudentList build() {
			return new PDFStudentList(this);
		}

	}
}
