package de.dhbw.humbuch.viewmodel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;

import de.davherrmann.mvvm.ActionHandler;
import de.davherrmann.mvvm.BasicState;
import de.davherrmann.mvvm.State;
import de.davherrmann.mvvm.annotations.AfterVMBinding;
import de.davherrmann.mvvm.annotations.HandlesAction;
import de.davherrmann.mvvm.annotations.ProvidesState;
import de.dhbw.humbuch.model.DAO;
import de.dhbw.humbuch.model.entity.BorrowedMaterial;
import de.dhbw.humbuch.model.entity.Grade;
import de.dhbw.humbuch.model.entity.SchoolYear;
import de.dhbw.humbuch.model.entity.Student;
import de.dhbw.humbuch.model.entity.TeachingMaterial;

/**
 * @author David Vitt
 *
 */
public class LendingViewModel {

	public interface GenerateMaterialListGrades extends ActionHandler {};
	public interface SetBorrowedMaterialsReceived extends ActionHandler {};
	public interface DoManualLending extends ActionHandler {};
	
	public interface StudentsWithUnreceivedBorrowedMaterials extends State<Map<Grade, Map<Student, List<BorrowedMaterial>>>> {};
	public interface MaterialListGrades extends State<Map<Grade, Map<TeachingMaterial, Integer>>> {};
	public interface TeachingMaterials extends State<Collection<TeachingMaterial>> {};

	@ProvidesState(StudentsWithUnreceivedBorrowedMaterials.class)
	public State<Map<Grade, Map<Student, List<BorrowedMaterial>>>> studentsWithUnreceivedBorrowedMaterials = new BasicState<>(Map.class);
	
	@ProvidesState(MaterialListGrades.class)
	public State<Map<Grade, Map<TeachingMaterial, Integer>>> materialListGrades = new BasicState<>(Map.class);

	@ProvidesState(TeachingMaterials.class)
	public State<Collection<TeachingMaterial>> teachingMaterials = new BasicState<>(Collection.class);
	
	private DAO<Grade> daoGrade;
	private DAO<Student> daoStudent;
	private DAO<TeachingMaterial> daoTeachingMaterial;
	private DAO<BorrowedMaterial> daoBorrowedMaterial;
	private DAO<SchoolYear> daoSchoolYear; 
	
	private SchoolYear recentlyActiveSchoolYear;
	
	/**
	 * Constructor
	 * 
	 * @param daoStudent
	 * @param daoTeachingMaterial
	 * @param daoGrade
	 * @param daoBorrowedMaterial
	 * @param daoSchoolYear
	 */
	@Inject
	public LendingViewModel(DAO<Student> daoStudent, DAO<TeachingMaterial> daoTeachingMaterial, DAO<Grade> daoGrade, 
			DAO<BorrowedMaterial> daoBorrowedMaterial, DAO<SchoolYear> daoSchoolYear) {
		this.daoStudent = daoStudent;
		this.daoTeachingMaterial = daoTeachingMaterial;
		this.daoGrade = daoGrade;
		this.daoBorrowedMaterial = daoBorrowedMaterial;
		this.daoSchoolYear = daoSchoolYear;
	}
	
	@AfterVMBinding
	public void refresh() {
		updateSchoolYear();
		updateTeachingMaterials();
		updateAllStudentsBorrowedMaterials();
	}
	
	/**
	 * Generates "list" of all {@link TeachingMaterial}s required by the given {@link Grade}s.<br>
	 * The "list" is returned as {@code Map<Grade, Map<TeachingMaterial, Integer>>} in the state {@link MaterialListGrades}
	 * 
	 * @param selectedGrades {@link Set} of {@link Grade}s
	 */
	@HandlesAction(GenerateMaterialListGrades.class)
	public void generateMaterialListGrades(Set<Grade> selectedGrades) {
		Map<Grade, Map<TeachingMaterial, Integer>> materialList = new TreeMap<Grade, Map<TeachingMaterial, Integer>>();
		
		for (Grade grade : selectedGrades) {
			Map<TeachingMaterial, Integer> gradeMap = new TreeMap<TeachingMaterial, Integer>();
			
			for(Student student : daoStudent.findAllWithCriteria(Restrictions.eq("leavingSchool", false), Restrictions.eq("grade", grade))) {
				for(BorrowedMaterial borrowedMaterial : student.getUnreceivedBorrowedList()) {
					TeachingMaterial teachingMaterial = borrowedMaterial.getTeachingMaterial();
					if(gradeMap.containsKey(teachingMaterial)) {
						gradeMap.put(teachingMaterial, gradeMap.get(teachingMaterial) + 1);
					} else {
						gradeMap.put(teachingMaterial, 1);
					}
				}
			}
			
			materialList.put(grade, gradeMap);
		}
		
		materialListGrades.set(materialList);
	}
	
	/**
	 * Marks the given {@link BorrowedMaterial}s as {@code received}.
	 * 
	 * @param borrowedMaterials {@link Collection} of {@link BorrowedMaterial}s that should be marked {@code received} 
	 */
	@HandlesAction(SetBorrowedMaterialsReceived.class)
	public void setBorrowedMaterialsReceived(Collection<BorrowedMaterial> borrowedMaterials) {
		for (BorrowedMaterial borrowedMaterial : borrowedMaterials) {
			borrowedMaterial.setReceived(true);
		}
		daoBorrowedMaterial.update(borrowedMaterials);
		
		updateAllStudentsBorrowedMaterials();
	}
	
	/**
	 * Lends the given {@link TeachingMaterial} for the given {@link Student} until the given {@link Date}.
	 * 
	 * @param student {@link Student} to lend this...
	 * @param toLend {@link TeachingMaterial} ...
	 * @param borrowUntil until this {@link Date}
	 */
	@HandlesAction(DoManualLending.class)
	public void doManualLending(Student student, TeachingMaterial toLend, Date borrowUntil) {
		persistBorrowedMaterial(student, toLend, borrowUntil);
		updateAllStudentsBorrowedMaterials();
	}
	
	/**
	 * Updates the list of {@link BorrowedMaterial}s for all {@link Student}s
	 */
	private void updateAllStudentsBorrowedMaterials() {
		Map<Student, List<TeachingMaterial>> newTeachingMaterials = getNewTeachingMaterials(daoGrade.findAll());
		for(Student student : newTeachingMaterials.keySet()) {
			persistBorrowedMaterials(student, newTeachingMaterials.get(student));
		}
		
		updateUnreceivedBorrowedMaterialsState();
	}

	/**
	 * Updates the {@link State} {@link TeachingMaterials}
	 */
	private void updateTeachingMaterials() {
		teachingMaterials.set(daoTeachingMaterial.findAll());
	}
	
	/**
	 * Updates the recently actice {@link SchoolYear}
	 */
	private void updateSchoolYear() {
		recentlyActiveSchoolYear = daoSchoolYear.findSingleWithCriteria(
				Order.desc("toDate"),
				Restrictions.le("fromDate", new Date()));
		if(recentlyActiveSchoolYear == null) {
			recentlyActiveSchoolYear = new SchoolYear.Builder(
					"now", getDate(Calendar.AUGUST, 1), getDate(Calendar.JUNE, 31))
			.endFirstTerm(getDate(Calendar.JANUARY, 31))
			.beginSecondTerm(getDate(Calendar.FEBRUARY, 1))
			.build();
		}
	}
	
	/**
	 * Returns {@link Date} object with the current year and the given month and day.
	 * 
	 * @param month
	 * @param day
	 * @return {@link Date} object with given information
	 */
	private Date getDate(int month, int day) {
		Calendar  calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR), month, day);
		return calendar.getTime();
	}
	
	/**
	 * Updates the {@link State} of all {@link Student}s unreceived {@link BorrowedMaterial}s.<br>
	 * Returned as {@code Map<Grade, Map<Student, List<BorrowedMaterial>>>} in {@link StudentsWithUnreceivedBorrowedMaterials}
	 * 
	 */
	private void updateUnreceivedBorrowedMaterialsState() {
		Map<Grade, Map<Student, List<BorrowedMaterial>>> unreceivedMap = new TreeMap<Grade, Map<Student, List<BorrowedMaterial>>>();
		
		for (Grade grade : daoGrade.findAll()) {
			Map<Student, List<BorrowedMaterial>> studentsWithUnreceivedBorrowedMaterials = new TreeMap<Student, List<BorrowedMaterial>>();
			for (Student student : grade.getStudents()) {
				if (student.hasUnreceivedBorrowedMaterials()) {
					List<BorrowedMaterial> unreceivedBorrowedList = student.getUnreceivedBorrowedList();
					Collections.sort(unreceivedBorrowedList);
					studentsWithUnreceivedBorrowedMaterials.put(student, unreceivedBorrowedList);
				}
			}
			
			if(!studentsWithUnreceivedBorrowedMaterials.isEmpty()) {
				unreceivedMap.put(grade, studentsWithUnreceivedBorrowedMaterials);
			}
		}

		studentsWithUnreceivedBorrowedMaterials.set(unreceivedMap);
	}
	
	/**
	 * Returns list of {@link TeachingMaterial}s that have to be lended by the given {@link Student}
	 * 
	 * @param student
	 * @return {@link Collection} of {@link TeachingMaterial}s that have to be lended
	 */
//	private List<TeachingMaterial> getNewTeachingMaterials(Student student) {
//		Collection<TeachingMaterial> teachingMaterials = daoTeachingMaterial.findAllWithCriteria(
//				Restrictions.and(
//						Restrictions.le("fromGrade", student.getGrade().getGrade())
//						, Restrictions.ge("toGrade", student.getGrade().getGrade())
//						, Restrictions.le("validFrom", new Date())
//						, Restrictions.le("fromTerm", recentlyActiveSchoolYear.getRecentlyActiveTerm())
//						, Restrictions.ge("toTerm", recentlyActiveSchoolYear.getRecentlyActiveTerm())
//						, Restrictions.or(
//								Restrictions.ge("validUntil", new Date())
//								, Restrictions.isNull("validUntil"))
//				));
//
//		List<TeachingMaterial> owningTeachingMaterials = getOwningTeachingMaterials(student);
//		List<TeachingMaterial> toLend = new ArrayList<TeachingMaterial>();
//
//		for(TeachingMaterial teachingMaterial : teachingMaterials) {
//			if(student.getProfile().containsAll(teachingMaterial.getProfile())
//					&& !owningTeachingMaterials.contains(teachingMaterial)
//					&& !recentlyActiveSchoolYear.getToDate().before(new Date())) {
//				toLend.add(teachingMaterial);
//			}
//		}
//
//		return toLend;
//	}
	
	/**
	 * Returns map of {@link TeachingMaterial}s that have to be lended by the {@link Student}s of the given {@link Grade}s.
	 * 
	 * @param grades
	 * @return {@link Map} of {@link Student}s and their {@link TeachingMaterial}s that have to be lended
	 */
	private Map<Student, List<TeachingMaterial>> getNewTeachingMaterials(List<Grade> grades) {
//		LOG.info("getNewTeachingMaterials()_start");
		Map<Student, List<TeachingMaterial>> studentsNewTeachingMaterialMap = new HashMap<Student, List<TeachingMaterial>>();
		
		for (Grade grade : grades) {
//			LOG.info("   grade: " + grade.toString());
			Collection<TeachingMaterial> teachingMaterials = daoTeachingMaterial.findAllWithCriteria(
					Restrictions.and(
							Restrictions.le("fromGrade", grade.getGrade())
							, Restrictions.ge("toGrade", grade.getGrade())
							, Restrictions.le("validFrom", new Date())
							, Restrictions.le("fromTerm", recentlyActiveSchoolYear.getRecentlyActiveTerm())
							, Restrictions.ge("toTerm", recentlyActiveSchoolYear.getRecentlyActiveTerm())
							, Restrictions.or(
									Restrictions.ge("validUntil", new Date())
									, Restrictions.isNull("validUntil"))
					));
			
			Date today = new Date();
			for (Student student : grade.getStudents()) {
				if (!student.isLeavingSchool()) {
					List<TeachingMaterial> toLend = new ArrayList<TeachingMaterial>();

					for (TeachingMaterial teachingMaterial : teachingMaterials) {
						if (student.getProfile().containsAll(teachingMaterial.getProfile())
								&& !getOwningTeachingMaterials(student).contains(teachingMaterial)
								&& !recentlyActiveSchoolYear.getToDate().before(today)) {
							toLend.add(teachingMaterial);
						}
					}
					
					studentsNewTeachingMaterialMap.put(student, toLend);
				}
			}
		}
		
//		LOG.info("getNewTeachingMaterials()_end");
		return studentsNewTeachingMaterialMap;
	}
	
	/**
	 * Persists the {@link TeachingMaterial}s for the {@link Student} as {@link BorrowedMaterial}s in the database.
	 * 
	 * @param student
	 * @param teachingMaterials
	 */
	private void persistBorrowedMaterials(Student student, List<TeachingMaterial> teachingMaterials) {
		Collection<BorrowedMaterial> borrowedMaterials = new ArrayList<>();
		for (TeachingMaterial teachingMaterial : teachingMaterials) {
			borrowedMaterials.add(new BorrowedMaterial.Builder(student, teachingMaterial, new Date()).build());
		}
		
		daoBorrowedMaterial.insert(borrowedMaterials);
	}
	
	/**
	 * Persists a single {@link TeachingMaterial} as {@link BorrowedMaterial}.<br>
	 * Is needed for manual lending.
	 * 
	 * @param student
	 * @param teachingMaterial
	 * @param borrowUntil
	 */
	private void persistBorrowedMaterial(Student student, TeachingMaterial teachingMaterial, Date borrowUntil) {
		daoBorrowedMaterial.insert(new BorrowedMaterial.Builder(student, teachingMaterial, new Date()).borrowUntil(borrowUntil).received(true).build());
	}
	
	/**
	 * Returns list of all {@link TeachingMaterial}s owned by the given {@link Student}.
	 * 
	 * @param student
	 * @return {@link List} of the {@link Student}s {@link TeachingMaterial}s
	 */
	private List<TeachingMaterial> getOwningTeachingMaterials(Student student) {
		List<TeachingMaterial> owning = new ArrayList<TeachingMaterial>();
		for(BorrowedMaterial borrowedMaterial : student.getBorrowedList()) {
			owning.add(borrowedMaterial.getTeachingMaterial());
		}
		
		return owning;
	}
}
