package de.dhbw.humbuch.model.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="grade")
public class Grade implements de.dhbw.humbuch.model.entity.Entity {

	@Id
	private int id;
	
	private int grade;
	
	@Column(name="suffix")
	private String suffix;
	
	private String teacher;
	
	@OneToMany(mappedBy="grade")
	private List<Student> students = new ArrayList<>();

	public Grade() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}

	public String getTeacher() {
		return teacher;
	}

	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	
	public static class Builder {
		private final int grade;
		private final String suffix;
		
		private String teacher;
		
		public Builder(int grade, String suffix) {
			this.grade = grade;
			this.suffix = suffix;
		}
		
		public Builder techer(String teacher) {
			this.teacher = teacher;
			return this;
		}
		
		public Grade build() {
			return new Grade(this);
		}
	}
	
	private Grade(Builder builder) {
		grade = builder.grade;
		suffix = builder.suffix;
		
		teacher = builder.teacher;
	}
}
