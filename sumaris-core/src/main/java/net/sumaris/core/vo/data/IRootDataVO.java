package net.sumaris.core.vo.data;

import net.sumaris.core.model.data.IWithRecorderPersonEntity;
import net.sumaris.core.vo.administration.programStrategy.ProgramVO;
import net.sumaris.core.vo.administration.user.PersonVO;

import java.io.Serializable;
import java.util.Date;

public interface IRootDataVO<T extends Serializable>
        extends IDataVO<T>,
        IWithRecorderPersonEntity<T, PersonVO> {

    ProgramVO getProgram();

    void setProgram(ProgramVO program);

    Date getCreationDate() ;

    void setCreationDate(Date creationDate);

    String getComments();

    void setComments(String comments);

    Date getValidationDate() ;

    void setValidationDate(Date validationDate);
}