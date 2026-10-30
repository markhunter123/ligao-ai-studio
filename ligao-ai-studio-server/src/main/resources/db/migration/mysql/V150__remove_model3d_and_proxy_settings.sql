-- V149: Remove the 3D-model generation feature and the global outbound-proxy
-- settings (both dropped from the product).
--
-- 1. ligao_model_config rows seeded by V72 for the hunyuan-3d provider.
-- 2. ligao_tool row for the Model3dGenerateTool bean (no longer exists).
-- 3. system settings keys used by Model3D settings page and ProxyManager.

DELETE FROM ligao_model_config WHERE model_type = 'model3d';
DELETE FROM ligao_tool WHERE name = 'Model3dGenerateTool';
DELETE FROM ligao_system_setting WHERE setting_key IN (
    'model3dEnabled', 'model3dProvider', 'model3dFallbackEnabled',
    'proxy.enabled', 'proxy.url', 'proxy.nonProxyHosts'
);
