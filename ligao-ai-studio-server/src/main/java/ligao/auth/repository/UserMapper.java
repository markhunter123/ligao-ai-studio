package ligao.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.auth.model.UserEntity;

/**
 * 用户 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
