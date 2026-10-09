package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ligao.wiki.dto.RawTitleRef;
import ligao.wiki.model.WikiRawMaterialEntity;

import java.util.Collection;
import java.util.List;

/**
 * Wiki raw material mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiRawMaterialMapper extends BaseMapper<WikiRawMaterialEntity> {

    /**
     * RFC-032: Batch-fetch raw material titles by IDs (fixes N+1 in wiki_semantic_search).
     */
    @Select("<script>SELECT id, title FROM ligao_wiki_raw_material " +
            "WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND deleted = 0</script>")
    List<RawTitleRef> selectBatchTitles(@Param("ids") Collection<Long> ids);
}
