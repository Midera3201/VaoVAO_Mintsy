package repository;

import controller.Employe;

import com.framework.util.Repository;

public class EmpRepository extends Repository<Employe> {

    @Override
    protected String getTableName() {
        return "employes";
    }

    @Override
    protected Class<Employe> getEntityClass() {
        return Employe.class;
    }
}
