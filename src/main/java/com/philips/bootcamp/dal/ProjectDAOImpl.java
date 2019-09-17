package com.philips.bootcamp.dal;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.utils.GenericUtils;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class ProjectDAOImpl implements ProjectDAO {
    
    @PersistenceContext
	EntityManager em;
	
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

    @Override
	public Project save(Project project) {
		em.persist(project);
		return project;
	}
    
    @Override
	public Project find(String name) {
		Project project = em.find(Project.class, name);
		return project;
	}
    
    @Override
	public void delete(String name) {
        Project p = em.find(Project.class, name);
		if (p != null) {
			em.remove(p);
		}
	}

	@Override
	public List<Project> findAll() {
		return GenericUtils.castList(Project.class, em.createQuery("select p from Project p").getResultList());
	}

	@Override
	public void update(Project project) {
		if (project == null) return;
		em.merge(project);
	}
}
