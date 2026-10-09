package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.wiki.model.WikiChunkEntity;

/**
 * Wiki chunk 数据访问层
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiChunkMapper extends BaseMapper<WikiChunkEntity> {
}
