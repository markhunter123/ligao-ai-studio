package ligao.datasource.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.datasource.model.DatasourceEntity;

/**
 * 数据源 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface DatasourceMapper extends BaseMapper<DatasourceEntity> {
}
