package ligao.workspace.conversation.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.workspace.conversation.model.ConversationEntity;

/**
 * 会话 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {
}
