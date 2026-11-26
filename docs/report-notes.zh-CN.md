
### 5.3 系统集成测试

| 测试 ID | 输入 / 操作 | 预期输出 | 实际结果 | 类别 | 状态 |
|---|---|---|---|---|---|
| TC-01 | 在 Dashboard 选择 Kuala Lumpur，点击 Refresh | AQI 卡片显示站点名、数值与等级；状态栏显示加载完成 | 与预期一致 | 正常 API 调用 | PASS |
| TC-02 | 打开 Stations 页，加载目录，搜索 "Kapit"，双击该行 | 表格过滤为一条记录；下方详情显示 AQI 25、等级 Good、样本数 | 与预期一致 | GUI 操作 | PASS |
| TC-03 | 将客户端指向本地桩服务，桩服务对所有请求返回 500 | 顶部显示"The air-quality service is unavailable. Showing the last saved reading."；若有缓存则继续显示旧读数 | 与预期一致 | 失败 API 响应 | PASS |
| TC-04 | 在 Alerts 页把阈值设为 Moderate or worse，返回 Dashboard 刷新至 AQI > 50 的分区 | 状态栏出现 "ALERT: Moderate (AQI …)"；Alerts 页历史新增一条；同一波段再次刷新不重复 | 与预期一致 | 端到端逻辑 | PASS |
| TC-05 | 在 Reports 页不填描述直接提交；随后填写 501 字描述再次提交 | 第一次提示"Describe what you observed."；第二次提示"Keep the description under 500 characters."；均不写入表格 | 与预期一致 | 边界情况 | PASS |

---
