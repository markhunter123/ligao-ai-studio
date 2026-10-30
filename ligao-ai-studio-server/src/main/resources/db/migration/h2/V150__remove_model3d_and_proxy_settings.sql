-- V149 (H2 dialect): see mysql/V149__remove_model3d_and_proxy_settings.sql.

DELETE FROM ligao_model_config WHERE model_type = 'model3d';
DELETE FROM ligao_tool WHERE name = 'Model3dGenerateTool';
DELETE FROM ligao_system_setting WHERE setting_key IN (
    'model3dEnabled', 'model3dProvider', 'model3dFallbackEnabled',
    'proxy.enabled', 'proxy.url', 'proxy.nonProxyHosts'
);
