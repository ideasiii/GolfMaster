/**
 * ShortTableManager 類別
 * 負責處理短桿分析的數據顯示，包括統計表格和落點分佈圖。
 */
class ShortTableManager {

    /**
     * 靜態配置：定義要顯示的統計數據及其來源。
     * 數值顯示的順序取決於此陣列的順序。
     * @property {string} id - HTML元素的ID。
     * @property {string} label - 統計數據的中文標籤。
     * @property {string} sourceKey - 數據 JSON 中的 key 名稱。
     * @property {function} formatter - 用於格式化顯示數值的函式。
     */
    static STAT_CONFIG = [
        {
            id: 'avgCarryDist',
            label: '平均落點 (yards)',
            sourceKey: 'avg_carry_dist_yd',
            // 取小數點後 1 位
            formatter: (value) => value.toFixed(1) + ' y'
        },
        {
            id: 'rollRatio',
            label: '滾動比 (%)',
            sourceKey: 'avg_roll_ratio',
            // 取整數
            formatter: (value) => Math.round(value) + ' %'
        },
        {
            id: 'stdevVertical',
            label: '垂直標準差 (yards)',
            sourceKey: 'stdev_carry_yd',
            // 取小數點後 1 位
            // formatter: (value) => value.toFixed(1)
            formatter: (value) => value ? value.toFixed(1) + ' y' : 'N/A' // 增加防呆
        },
        {
            id: 'stdevHorizontal',
            label: '水平標準差 (yards)',
            sourceKey: 'stdev_horizontal_yd',
            // 格式化函式會處理方向標註 (L/R)
            formatter: (value, data) => {
                // 增加檢查，如果 value 不存在則直接返回 N/A
                if (value === undefined || value === null) {
                    return 'N/A';
                }
                // 判斷平均水平發射方向 (avg_horizontal_deviation_yd)
                const direction = data.avg_horizontal_deviation_yd > 0 ? 'R' : 'L';
                return `${direction}${value.toFixed(1)} y`;
            }
        },
        // 未來擴充數據只需在這裡新增配置即可
    ];


    /**
     * 建構子，初始化 DOM 元素的參考並動態建立統計表格行。
     * @param {string} tableContainerId - 統計表格最外層容器的 ID，例如 'shotAnalysisBox'。
     */
    constructor(tableContainerId) {
        this.containerElement = document.getElementById(tableContainerId);

        // 儲存當前的分析數據 (用於重繪圖表)
        this.currentAnalysisData = null;
        // 目標距離，預設為 50 碼
        this.targetDistanceYd = 50;

        if (!this.containerElement) {
            console.error(`ShortTableManager：找不到 ID 為 ${tableContainerId} 的容器。`);
            return;
        }

        // 查找統計表格和落點圖的容器
        this.statsTableElement = this.containerElement.querySelector('#shotStatsTable');
        this.elements = {
            dispersionMap: this.containerElement.querySelector('#shotDispersionMap') // 落點分佈圖容器
        };

        // 動態生成表格結構並儲存數值元素的參考
        this._generateTableRows();

        // 初始化目標距離的事件監聽
        this._initTargetControl();

        // 檢查落點圖元素是否找到
        if (!this.elements.dispersionMap) {
            console.warn(`ShortTableManager：找不到 ID 為 #shotDispersionMap 的元素。`);
        }
    }

    /**
     * 【新增方法】設定目標距離輸入框的事件監聽。
     */
    _initTargetControl() {
        // 【注意】這裡假設 targetDistanceInput 元素已經存在於 DOM 中，如果沒有需要手動添加到 this.elements
        // 假設目標距離輸入框的 ID 為 'targetDistanceInput'
        const input = this.containerElement.querySelector('#targetDistanceInput');
        this.elements.targetDistanceInput = input; // 將元素參考儲存起來

        if (input) {
            // 從輸入框取得初始值並更新屬性
            this.targetDistanceYd = Number(input.value) || 50;

            input.addEventListener('change', () => {
                // 1. 取得新值，確保為正數
                let newValue = Math.max(1, Math.round(Number(input.value)));

                // 2. 更新輸入框的值 (防止負數或小數點)
                input.value = newValue;

                // 3. 更新類別屬性
                this.targetDistanceYd = newValue;

                // 4. 重繪圖表，如果已經有數據的話
                if (this.currentAnalysisData && this.dispersionChart) {
                    this.drawDispersionMap(this.currentAnalysisData, {
                        showEllipse: true,
                        showTarget: true // 確保目標圓顯示
                    });
                }
            });
        }
    }

    /**
     * 根據 STAT_CONFIG 動態建立表格的 HTML 結構 (stat-row)。
     */
    _generateTableRows() {
        if (!this.statsTableElement) return;

        // 清空現有內容，準備重新建立
        this.statsTableElement.innerHTML = '';

        ShortTableManager.STAT_CONFIG.forEach(config => {
            // 創建 <div class="stat-row">
            const row = document.createElement('div');
            row.className = 'stat-row';

            // 創建 <span class="stat-label">
            const label = document.createElement('span');
            label.className = 'stat-label';
            label.textContent = config.label;

            // 創建 <span class="stat-value highlight-value">
            const value = document.createElement('span');
            value.className = 'stat-value highlight-value';
            value.id = config.id; // 設定 ID
            value.textContent = 'N/A'; // 預設值

            // 組合結構
            row.appendChild(label);
            row.appendChild(value);

            // 添加到表格容器中
            this.statsTableElement.appendChild(row);

            // 將數值元素儲存到 this.elements 中以供後續更新
            this.elements[config.id] = value;
        });
    }

    /**
     * 根據數據物件更新表格中的統計數值。
     * @param {string} dataString - 包含分析數據的 JSON 字串。
     */
    updateTable(dataString) {
        let data;

        try {
            data = JSON.parse(dataString);
        } catch (error) {
            console.error("ShortTableManager：解析 JSON 字串失敗。", error);
            this.clearTable();
            return;
        }

        if (data.status !== 'success') {
             console.warn("ShortTableManager：分析數據狀態非 success。不更新表格。", data.message);
             this.clearTable();
             return;
        }

        this._printData(data);

        // 儲存當前的數據，以便重繪圖表時使用
        this.currentAnalysisData = data;

        // 1. 遍歷配置清單，更新 DOM 元素
        ShortTableManager.STAT_CONFIG.forEach(config => {
            const valueElement = this.elements[config.id];
            const rawValue = data[config.sourceKey]; // 從數據中取得原始數值

            if (valueElement && rawValue !== undefined) {
                // 使用配置中定義的 formatter 函式來格式化數值
                const formattedValue = config.formatter(rawValue, data);
                valueElement.textContent = formattedValue;
            } else if (valueElement) {
                 // 如果數據中缺少這個 key，則顯示 N/A
                 valueElement.textContent = 'N/A';
            }
        });

        // 2. 呼叫繪圖函式 (保留現有邏輯)
        this.drawDispersionMap(data, {
            showEllipse: true,
            showTarget: false // 這裡的 showTarget 控制的是 Annotation (同心圓)
        });
    }

    /**
     * 清空或重設表格顯示。
     */
    clearTable() {
        // 遍歷所有統計數據 ID，並將其內容設為 N/A
        ShortTableManager.STAT_CONFIG.forEach(config => {
            const element = this.elements[config.id];
            if (element) {
                element.textContent = 'N/A';
            }
        });
        if (this.elements.dispersionMap) this.elements.dispersionMap.innerHTML = '';
    }

    /**
     * 顯示數據
     */
    _printData(analysisData) {
        console.log(
            "total_shots = " + analysisData.total_shots,
            "analyzed_shots = " + analysisData.analyzed_shots,
            "club_type = " + analysisData.club_type,
            "avg_carry_dist_yd = " + analysisData.avg_carry_dist_yd,
            "avg_horizontal_deviation_yd = " + analysisData.avg_horizontal_deviation_yd,
            "avg_launch_direction_deg = " + analysisData.avg_launch_direction_deg,
            "stdev_carry_yd = " + analysisData.stdev_carry_yd,
            "stdev_horizontal_yd = " + analysisData.stdev_horizontal_yd,
            "avg_carry_ratio = " + analysisData.avg_carry_ratio,
            "avg_roll_ratio = " + analysisData.avg_roll_ratio,
            "landing_consistency_percent = " + analysisData.landing_consistency_percent,
            "max_deviation_yd = " + analysisData.max_deviation_yd,
            "covariance_xy = " + analysisData.covariance_xy,
        );
    }

    /**
     * 繪製落點分佈圖 (Chart.js 散點圖)。
     * @param {object} analysisData - 包含 landing_points 和 avg_carry_dist_yd 等的完整數據物件。
     * @param {object} [options={}] - 視覺化選項。
     * @param {boolean} [options.showEllipse=true] - 是否顯示標準差橢圓。
     * @param {boolean} [options.showTarget=true] - 是否顯示同心目標圓。
     */
    drawDispersionMap(analysisData, options = {}) {
        const container = this.elements.dispersionMap;
        if (!container) return;

        // 設置預設選項
        const defaultOptions = {
            showEllipse: true,
            showTarget: true
        };
        options = { ...defaultOptions, ...options };

        // 1. 清理舊圖表並建立 Canvas
        container.innerHTML = '';
        const canvas = document.createElement('canvas');
        canvas.id = 'dispersionCanvas';
        container.appendChild(canvas);

        if (this.dispersionChart) {
            this.dispersionChart.destroy();
        }

        // 2. 準備所有配置
        const colors = this._getChartColors();
        const data = this._getChartData(analysisData, colors);
        const scales = this._calculateScales(analysisData, colors);
        const annotations = this._getChartAnnotations(analysisData, colors, options, this.targetDistanceYd);

        // 3. 繪製 Chart
        this.dispersionChart = new Chart(canvas, {
            type: 'scatter',
            data: data,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        labels: {
                            color: colors.colorText,
                            // 僅顯示擊球落點、最新擊球、95% CI 橢圓和目標點的圖例
                            filter: (legendItem, chartData) => {
                                const label = chartData.datasets[legendItem.datasetIndex].label;
                                return (
                                    label === '95% CI 橢圓' ||
                                    label === '擊球落點' ||
                                    label === '最新擊球' ||
                                    label === '目標' // 保留目標點的圖例
                                );
                            }
                        }
                    },
                    tooltip: {
                        titleColor: '#fff',
                        bodyColor: '#fff',
                        callbacks: {
                            // 保持 tooltip 格式不變 (R/L 偏差)
                            label: (context) => {
                                let label = context.dataset.label || '';
                                if (label) { label += ': '; }
                                const xVal = context.parsed.x;
                                const yVal = context.parsed.y.toFixed(1);

                                let xDir = '';
                                if (xVal > 0.1) {
                                    xDir = `R${xVal.toFixed(1)}`;
                                } else if (xVal < -0.1) {
                                    xDir = `L${Math.abs(xVal).toFixed(1)}`;
                                } else {
                                    xDir = `${xVal.toFixed(1)}`;
                                }
                                return `${label}(${xDir} yd, ${yVal} yd)`;
                            }
                        }
                    },
                    annotation: annotations
                },
                scales: scales
            }
        });
    }

    // ====================================
    // 繪製圖函式
    // =====================================

    /**
     * 定義 Chart.js 圖表使用的顏色變數。
     * @returns {object} 包含所有顏色代碼的物件。
     */
    _getChartColors() {
        return {
            colorNew: 'rgba(0, 255, 132, 1)', // 擊球落點：亮綠色
            colorPrimary: 'rgba(255, 206, 86, 0.9)', // 擊球落點：亮黃色
            colorAverage: 'rgba(54, 162, 235, 1)',  // 平均點：亮藍色
            colorText: 'rgba(255, 255, 255, 0.9)',  // 文字：白色
            colorGridLine: 'rgba(255, 255, 255, 0.2)', // 網格線：半透明白
            colorCenterLine: 'rgba(255, 255, 255, 0.7)', // 中心線：較亮白

            // 目標點
            colorGoal: 'rgba(255, 99, 132, 1)', // 1 碼目標：亮紅色

            // 橢圓
            colorSigma1Fill: 'rgba(54, 162, 235, 0.2)', // 1-Sigma 填充：淺藍透明
            colorSigma1Border: 'rgba(54, 162, 235, 0.9)', // 1-Sigma 邊框：亮藍
            colorSigma2Border: 'rgba(153, 102, 255, 0.7)', // 2-Sigma 邊框：淺紫色

            // 同心圓 (雖然現在不使用，但保留代碼供參考)
            colorTarget3Border: 'rgba(75, 192, 192, 0.9)',  // 3 碼目標：亮青色
            colorTarget1Border: 'rgba(255, 99, 132, 1)', // 1 碼目標：亮紅色
        };
    }

    /**
     * 計算圖表 X/Y 座標軸的範圍，並納入視覺平衡邏輯。
     * 【修正】確保 Y 軸範圍包含 TargetDistanceYd
     * @param {object} analysisData - 包含 avg_carry_dist_yd, stdev_carry_yd, stdev_horizontal_yd 的數據物件。
     * @param {object} colors - 顏色設定物件。
     * @returns {object} 包含 min/max 設定的 scales 物件，可直接用於 Chart.js options。
     */
    _calculateScales(analysisData, colors) {
        const avgCarry = analysisData.avg_carry_dist_yd;
        const stdevVertical = analysisData.stdev_carry_yd;
        const stdevHorizontal = analysisData.stdev_horizontal_yd;

        // 取得目標距離。
        const targetDistance = this.targetDistanceYd || 50;

        const STDEV_MULTIPLE = 3; // 3-sigma 涵蓋範圍
        const MIN_RANGE = 5;       // 最小軸心範圍 (保護數據過小時)
        const FORCED_RATIO = 0.3; // 強制 X 軸視覺至少為 Y 軸 3-sigma 範圍的 70%

        // 1. Y 軸 (垂直距離) 範圍計算
        const yStdevRange = Math.max(MIN_RANGE, stdevVertical * STDEV_MULTIPLE);
        const yRange = yStdevRange;

        // 確保 Y 軸範圍至少涵蓋：[平均點 - 範圍] ~ [平均點 + 範圍]
        let yMin = Math.max(0, avgCarry - yRange);
        let yMax = avgCarry + yRange;

        // 【修正】確保目標距離及其 3 碼圓也在 Y 軸視野內 (即使不顯示圓，也確保點在視野內)
        const targetMin = targetDistance - 3;
        const targetMax = targetDistance + 3;

        yMin = Math.min(yMin, targetMin);
        yMax = Math.max(yMax, targetMax);

        // 稍微增加緩衝，讓圖表更好看
        const yBuffer = (yMax - yMin) * 0.05;
        yMin = Math.max(0, yMin - yBuffer);
        yMax = yMax + yBuffer;


        // 2. X 軸 (水平偏差) 範圍計算 (包含視覺平衡)
        const xStdevRange = Math.max(MIN_RANGE, stdevHorizontal * STDEV_MULTIPLE);

        // 計算視覺所需的 X 軸絕對最大值
        const xMinAbsForced = (yMax - yMin) / 2 * FORCED_RATIO; // 使用新的 Y 軸總範圍計算 X 軸強制最小範圍
        const finalXMaxAbs = Math.max(xStdevRange, xMinAbsForced, 3.5); // 確保至少有 3 碼目標圓的視野

        return {
            x: {
                // ... (X 軸配置保持不變，使用 finalXMaxAbs) ...
                type: 'linear',
                position: 'bottom',
                title: { display: true, text: '水平偏差 (碼) - 左 | 右', color: colors.colorText },
                ticks: { color: colors.colorText },
                min: -finalXMaxAbs,
                max: finalXMaxAbs,
                grid: {
                    borderColor: colors.colorText,
                    borderWidth: 1,
                    drawOnChartArea: true,
                    color: (context) => context.tick.value === 0 ? colors.colorCenterLine : colors.colorGridLine,
                    lineWidth: (context) => context.tick.value === 0 ? 2 : 1,
                },
            },
            y: {
                type: 'linear',
                position: 'left',
                title: { display: true, text: '飛行距離 (碼)', color: colors.colorText },
                ticks: { color: colors.colorText },
                min: yMin, // 使用新的 Y 軸最小值
                max: yMax, // 使用新的 Y 軸最大值
                grid: {
                    color: colors.colorGridLine,
                },
            }
        };
    }

      /**
     * 建立 Chart.js Annotation 插件所需的配置物件 (目標圓)。
     * 【重要】此方法現在僅處理目標圓，**目標點**作為 Dataset 處理。
     * @param {object} analysisData - 包含所有統計數據 (需包含 covariance_xy)。
     * @param {object} colors - 顏色設定物件。
     * @param {object} options - 視覺化選項 (showEllipse, showTarget)。
     * @param {number} targetDistanceYd - 使用者設定的目標距離。
     * @returns {object} Annotation 配置物件。
     */
    _getChartAnnotations(analysisData, colors, options, targetDistanceYd) {
        // 由於我們不再繪製同心圓 Annotation，此方法現在返回一個空物件，
        // 除非未來需要新增其他 Annotation (例如：中線標註)。
        // 即使 options.showTarget 為 true，我們也不再繪製 Annotation circle。

        // 原本的邏輯：
        // const targetY = targetDistanceYd;
        // const annotations = {
        //     center: 0,
        //     yCenter: targetY,
        //     drawTime: 'afterDatasetsDraw',
        //     annotations: {}
        // };

        // // B. 目標圓 - 這部分已移除，因為您希望刪除同心圓
        // if (options.showTarget) {
        //     // ... 3 碼圓和 1 碼圓的配置 ...
        // }

        // 簡化為：
        return {};
    }

    /**
     * 【方法 A】計算 95% 信賴區間橢圓的邊界點 (特徵分解與卡方分佈)。
     * 這會生成一系列 (x, y) 點，用於繪製成一個 line dataset，以取代不精確的 Annotation Ellipse。
     * @param {object} analysisData - 包含 stdev_carry_yd, stdev_horizontal_yd, covariance_xy 的數據。
     * @returns {Array<{x: number, y: number}>} 橢圓邊界點陣列。
     */
    _calculateConfidenceEllipsePoints(analysisData) {
        const varX = analysisData.stdev_horizontal_yd ** 2; // X 軸方差 (水平)
        const varY = analysisData.stdev_carry_yd ** 2;      // Y 軸方差 (垂直)
        const covXY = analysisData.covariance_xy || 0;
        const avgCarry = analysisData.avg_carry_dist_yd || 0;
        const avgHorizontal = analysisData.avg_horizontal_deviation_yd || 0;

        // 雙變量高斯分佈 95% 信賴區間，2 自由度的卡方分佈 (Chi-Squared) 0.95 quantile
        // Chi-Squared (df=2, p=0.95) ≈ 5.991
        // Chi-Squared (df=2, p=0.5) ≈ 1.386
        const CHI2_QUANTILE_095 = 5.991;

        // 1. 計算協方差矩陣的特徵值 (Eigenvalues)
        const trace = varX + varY;
        const det = varX * varY - covXY ** 2;
        const term = Math.sqrt((trace * trace) / 4 - det);

        const lambda1 = trace / 2 + term; // 最大特徵值 (長軸)
        const lambda2 = trace / 2 - term; // 最小特徵值 (短軸)

        // 2. 計算長軸和短軸的長度 (半軸長)
        const majorAxis = Math.sqrt(lambda1 * CHI2_QUANTILE_095); // 長半軸
        const minorAxis = Math.sqrt(lambda2 * CHI2_QUANTILE_095); // 短半軸

        // 3. 計算主軸的旋轉角度
        let rotationRad = 0;
        if (covXY !== 0) {
            rotationRad = Math.atan2(2 * covXY, varX - varY) / 2;
        }

        const points = [];
        const numPoints = 60; // 邊界點的數量，越多越平滑

        // 4. 生成橢圓邊界點
        for (let i = 0; i <= numPoints; i++) {
            const angle = (i / numPoints) * 2 * Math.PI; // 0 到 2π

            // 參數方程座標 (以長軸/短軸為基準)
            const xPrime = majorAxis * Math.cos(angle); // 沿長軸
            const yPrime = minorAxis * Math.sin(angle); // 沿短軸

            // 旋轉和平移到圖表座標系
            const x = xPrime * Math.cos(rotationRad) - yPrime * Math.sin(rotationRad);
            const y = xPrime * Math.sin(rotationRad) + yPrime * Math.cos(rotationRad);

            // 加上平均點 (0, avgCarry)
            points.push({
                x: x + avgHorizontal,
                y: y + avgCarry
            });
        }

        return points;
    }

    /**
     * 根據擊球順序生成漸變顏色陣列。
     * 注意：此函式從索引 0 開始計算，但我們將在 _getChartData 中將索引 0 替換為特殊顏色。
     * 顏色將從淺色 (舊擊球) 漸變到深色 (新擊球，但不包含索引 0)。
     * @param {number} count - 擊球總數。
     * @param {string} baseColor - 基礎顏色 (例如: 'rgba(255, 206, 86, 0.9)')。
     * @returns {Array<string>} 每個擊球點的顏色代碼陣列。
     */
    _getSequentialColors(count, baseColor) {
        if (count <= 1) return new Array(count).fill(baseColor); // 至少 2 點才需要漸變

        const match = baseColor.match(/rgba\((\d+),\s*(\d+),\s*(\d+),\s*([\d.]+)\)/);
        if (!match) {
            return new Array(count).fill(baseColor);
        }

        const [_, r, g, b] = match.map(Number); // 忽略原始 alpha 值
        const colors = [];

        // 【調整】設定新的 Alpha 範圍
        const minAlpha = 0.2; // 較舊/較淡的最低透明度
        const maxAlpha = 0.6; // 較新/較實的最高透明度
        const alphaRange = maxAlpha - minAlpha;

        // 計算漸變點的數量：從索引 1 到 count-1 (共 count-1 點)
        const gradientCount = count - 1;

        // 索引 0 (最新點) 的顏色將在 _getChartData 中處理，這裡先加入佔位符
        colors.push(null);

        // 從索引 1 (次新點) 開始計算漸變
        for (let i = 1; i < count; i++) {
            // i=1 是次新點，i=count-1 是最舊點。
            // ratio: i=1 時接近 0 (新)，i=count-1 時接近 1 (舊)
            // 為了讓次新點 (i=1) 較實 (Alpha 大)，最舊點 (i=count-1) 較淡 (Alpha 小)，
            // 我們使用 (i - 1) / (gradientCount - 1) 作為比例。

            const ratio = (i - 1) / Math.max(1, gradientCount - 1);

            // 使用 1 - ratio: 讓次新點 (ratio 接近 0) 具有 maxAlpha。
            const currentAlpha = maxAlpha - (ratio * alphaRange);

            colors.push(`rgba(${r}, ${g}, ${b}, ${currentAlpha.toFixed(2)})`);
        }

        return colors;
    }

    /**
     * 準備 Chart.js 所需的數據集 (落點、目標點、平均點和信賴區間橢圓)。
     * @param {object} analysisData - 包含 landing_points 和 avg_carry_dist_yd 的數據物件。
     * @param {object} colors - 顏色設定物件。
     * @returns {object} Chart.js data 物件。
     */
    _getChartData(analysisData, colors) {
        const landingPoints = analysisData.landing_points;
        const avgCarry = analysisData.avg_carry_dist_yd;
        const avgHorizontal = analysisData.avg_horizontal_deviation_yd || 0;

        // 【取得目標距離】
        const targetDistance = this.targetDistanceYd;


        // 1. 準備擊球落點 (所有點)
        const allCarryPoints = landingPoints.x_coords_yd.map((x, index) => ({
            x: x,
            y: landingPoints.y_coords_yd[index]
        }));

        // 【分離】分離 最新一桿 (Last Shot) 和 舊點 (Previous Shots)
        const latestShot = allCarryPoints.length > 0 ? allCarryPoints[0] : null;
        const previousShots = allCarryPoints.slice(1);

        // 【計算】計算舊點的漸變顏色陣列
        // 舊點數量為 carryPoints.length - 1
        const sequenceColors = this._getSequentialColors(
            allCarryPoints.length, // 傳入總數，讓函式知道計算幾組顏色
            colors.colorPrimary
        ).slice(1); // 只需要從索引 1 開始的顏色 (最新的點顏色由我們手動設定)

        // 95% 信賴區間橢圓邊界點
        const ellipsePoints = this._calculateConfidenceEllipsePoints(analysisData);

        // *** 調整資料集順序和層級 (Order: 數字越小越在底層) ***
        const datasets = [
            // 0. 信賴區間橢圓
            {
                label: '95% CI 橢圓',
                data: ellipsePoints,
                borderColor: colors.colorSigma1Border,
                borderWidth: 3,
                fill: 'start',
                backgroundColor: colors.colorSigma1Fill,
                pointRadius: 0,
                showLine: true,
                type: 'line',
                tension: 0,
                order: 50,
            },
            // 1. 舊擊球落點
            {
                label: '擊球落點',
                data: previousShots,
                // 只使用漸變陣列 (排除了最新一桿的顏色)
                backgroundColor: sequenceColors,
                pointRadius: 6,
                pointHoverRadius: 8,
                pointBorderColor: 'rgba(0, 0, 0, 0.3)',
                pointBorderWidth: 1,
                tooltipHidden: false,
                type: 'scatter',
                order: 60, // 略低於橢圓
            },
            // 【保留】2. 目標點 (Target Point)
            // 使用 colors.colorGoal (亮紅色)
            {
                label: '目標',
                data: [{ x: 0, y: targetDistance }],
                backgroundColor: colors.colorGoal,
                pointRadius: 10, // 讓點比擊球點和平均點都大
                pointStyle: 'star', // 使用 'star' 樣式突出顯示
                pointBorderColor: colors.colorGoal,
                pointBorderWidth: 2,
                tooltipHidden: false,
                type: 'scatter',
                order: 5, // 確保在擊球點之上，平均點之下
            },
        ];

        // 3. 最新擊球
        if (latestShot) {
             datasets.push({
                label: '最新擊球', // 讓這個點的 Tooltip 標籤不同
                data: [latestShot],
                backgroundColor: colors.colorNew, // 假設 colorNew 是亮綠色
                pointRadius: 8, // 給予較大的半徑，強化可見性
                pointHoverRadius: 10,
                pointBorderColor: 'rgba(255, 255, 255, 0.8)', // 增加邊框，突出顯示
                pointBorderWidth: 2,
                tooltipHidden: false,
                type: 'scatter',
                order: 1, // 強制置頂
             });
        }

        // 4. 平均點
        datasets.push(
            {
                // label: '平均點', // 保持隱藏標籤
                data: [{ x: avgHorizontal, y: avgCarry }],
                backgroundColor: colors.colorAverage, // 保持藍色
                pointRadius: 8,
                pointStyle: 'crossRot',
                pointBorderColor: colors.colorAverage,
                pointBorderWidth: 2,
                showLine: false,
                tooltipHidden: true,
                type: 'scatter',
                order: 10,
            }
        );

        return { datasets };
    }
}

