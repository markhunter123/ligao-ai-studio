package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.wiki.model.WikiPageDependencyEntity;

/**
 * Mapper for {@link WikiPageDependencyEntity}.
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiPageDependencyMapper extends BaseMapper<WikiPageDependencyEntity> {
}
