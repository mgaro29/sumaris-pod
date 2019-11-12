package net.sumaris.core.model.social;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import net.sumaris.core.dao.technical.model.IUpdateDateEntityBean;

import javax.persistence.*;
import java.util.Date;


@Data
@FieldNameConstants
@Entity
@Table(name = "user_event")
@Cacheable
/**
 * TODO: complete this entity class
 */
public class UserEvent implements IUpdateDateEntityBean<Integer, Date> {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "USER_EVENT_SEQ")
    @SequenceGenerator(name = "USER_EVENT_SEQ", sequenceName="USER_EVENT_SEQ")
    private Integer id;

    @Column(name = "creation_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @Column(name = "issuer", nullable = false, length = 44)
    private String issuer;

    public String toString() {
        return new StringBuilder().append(super.toString()).append(",issuer=").append(this.issuer).toString();
    }
}