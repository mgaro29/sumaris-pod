package net.sumaris.core.extraction.service;

import net.sumaris.core.dao.technical.SortDirection;
import net.sumaris.core.extraction.vo.ExtractionFilterVO;
import net.sumaris.core.extraction.vo.ExtractionResultVO;
import net.sumaris.core.extraction.vo.ExtractionTypeVO;
import net.sumaris.core.vo.filter.TripFilterVO;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/**
 * @author peck7 on 17/12/2018.
 */
@Transactional
public interface ExtractionService {


    void executeToFile(TripFilterVO filter, File outputFile);

    @Transactional(readOnly = true)
    List<ExtractionTypeVO> getAllTypes();

    @Transactional(readOnly = true)
    ExtractionResultVO getRows(ExtractionTypeVO type,
                               ExtractionFilterVO filter,
                               int offset,
                               int size,
                               String sort,
                               SortDirection direction) ;


}
