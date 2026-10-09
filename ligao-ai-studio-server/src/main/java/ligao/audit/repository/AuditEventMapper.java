package ligao.audit.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.audit.model.AuditEventEntity;

@Mapper
public interface AuditEventMapper extends BaseMapper<AuditEventEntity> {
}
