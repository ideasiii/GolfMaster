/*
 * @fileoverview This script contains functions to initialize and configure a radar chart using Chart.js.
 * It is designed to visualize golf shot data by comparing user performance against predefined expert levels.
*/

/**
 * Calculates the performance level for a given metric value based on a set of ranges.
 * The levels are 1-based.
 * @param {number} value The value of the shot metric (e.g., backSpin, clubSpeed).
 * @param {Array<Array<number>>} range An array of [min, max] pairs defining the boundaries for each level.
 * @returns {number} The calculated performance level, from 1 to range.length.
 */
function getLevelForRadar(value, range) {
    // If the value is lower than the minimum defined range, it's considered level 1.
    if (value < range[0][0]) {
        return 1;
    }

    const level = range.findIndex(r => value >= r[0] && value <= r[1]) + 1;

    // If a level is found, return it.
    // If the value exceeds the top range, assign it the highest possible level.
    return level > 0 ? level : range.length;
}


/**
 * Initializes a radar chart to display golf shot analysis.
 * @param {HTMLCanvasElement} radarChartElement The canvas element where the chart will be drawn.
 * @param {object} userShotData An object containing the user's current and average shot data.
 * @param {object} expertLevels An object containing the level thresholds for different metrics.
 */
function initializeRadarChart(radarChartElement, userShotData, expertLevels) {
    // Early return if the canvas element is not valid.
    if (radarChartElement === undefined || radarChartElement === null) {
        console.warn("initializeRadarChart: radarChart is undefined or null");
        return;
    }

    // Alias for better readability
    const L = expertLevels;
    const shotData = userShotData;

    let ranges = {};
    let radarData = {};

    // console.log(L);
    // console.log(shotData);

    try{
        // Define the performance level ranges for each metric.
        // Each metric has 7 levels, from "Worse" to "Great".
        ranges = {
            'BackSpin': [
                [1, L.worseLevelLowBsp],
                [L.worseLevelLowBsp, L.badLevelLowBsp],
                [L.badLevelLowBsp, L.normalLevelLowBsp],
                [L.normalLevelLowBsp, L.goodLevelLowBsp],
                [L.goodLevelLowBsp, L.greatLevelLowBsp],
                [L.greatLevelLowBsp, L.greatLevelTopBsp],
                [L.greatLevelTopBsp, L.greatLevelTopBsp + shotData.backSpin]],
            'ClubSpeed': [
                [1, L.worseLevelLowCS],
                [L.worseLevelLowCS, L.badLevelLowCS],
                [L.badLevelLowCS, L.normalLevelLowCS],
                [L.normalLevelLowCS, L.goodLevelLowCS],
                [L.goodLevelLowCS, L.greatLevelLowCS],
                [L.greatLevelLowCS, L.greatLevelTopCS],
                [L.greatLevelTopCS, L.greatLevelTopCS + shotData.clubSpeed]],
            'Distance':[
                [1, L.worseLevelLowDist],
                [L.worseLevelLowDist, L.badLevelLowDist],
                [L.badLevelLowDist, L.normalLevelLowDist],
                [L.normalLevelLowDist, L.goodLevelLowDist],
                [L.goodLevelLowDist, L.greatLevelLowDist],
                [L.greatLevelLowDist, L.greatLevelTopDist],
                [L.greatLevelTopDist, L.greatLevelTopDist + shotData.distance]],
            'BallSpeed': [
                [1, L.worseLevelLowBS],
                [L.worseLevelLowBS, L.badLevelLowBS],
                [L.badLevelLowBS, L.normalLevelLowBS],
                [L.normalLevelLowBS, L.goodLevelLowBS],
                [L.goodLevelLowBS, L.greatLevelLowBS],
                [L.greatLevelLowBS, L.greatLevelTopBS],
                [L.greatLevelTopBS, L.greatLevelTopBS + shotData.ballSpeed]],
            'LaunchAngle': [
                [1, L.worseLevelLowLA],
                [L.worseLevelLowLA, L.badLevelLowLA],
                [L.badLevelLowLA, L.normalLevelLowLA],
                [L.normalLevelLowLA, L.goodLevelLowLA],
                [L.goodLevelLowLA, L.greatLevelLowLA],
                [L.greatLevelLowLA, L.greatLevelTopLA],
                [L.greatLevelTopLA, L.greatLevelTopLA + shotData.launchAngle]],
        };

        radarData = {
            labels: ['後旋', '桿頭速度', '距離', '球速', '發射角度'],
            datasets: [{
                label: '本次擊球',
                data: [
                    getLevelForRadar(shotData.backSpin, ranges.BackSpin),
                    getLevelForRadar(shotData.clubSpeed, ranges.ClubSpeed),
                    getLevelForRadar(shotData.distance, ranges.Distance),
                    getLevelForRadar(shotData.ballSpeed, ranges.BallSpeed),
                    getLevelForRadar(shotData.launchAngle, ranges.LaunchAngle)
                ],
                backgroundColor: 'rgba(0, 169, 188, 0.2)',
                borderColor: 'rgba(0, 169, 188, 1)',
                pointBackgroundColor: 'rgba(0, 169, 188, 1)',
                pointBorderColor: '#fff',
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: 'rgba(0, 169, 188, 1)'
            }, {
                label: '擊球歷程',
                data: [
                    getLevelForRadar(shotData.avgBsp, ranges.BackSpin),
                    getLevelForRadar(shotData.avgCS, ranges.ClubSpeed),
                    getLevelForRadar(shotData.avgDist, ranges.Distance),
                    getLevelForRadar(shotData.avgBS, ranges.BallSpeed),
                    getLevelForRadar(shotData.avgLA, ranges.LaunchAngle)
                ],
                backgroundColor: "rgba(240, 129, 86, 0.2)",
                borderColor: "rgba(240, 129, 86, 1)",
                pointBackgroundColor: "rgba(240, 129, 86, 1)",
                pointBorderColor: "#fff",
                pointHoverBackgroundColor: "#fff",
                pointHoverBorderColor: "rgba(240, 129, 86, 1)"
            }, {
                // This hidden dataset is a trick to ensure the radar chart's scale is always from 1 to 7.
                label: '', // Hidden dataset to enforce scale
                data: [1, 2, 3, 4, 5, 6, 7], // Covers the data range from 1 to 7
                borderColor: 'rgba(0, 0, 0, 0)', // Fully transparent
                backgroundColor: 'rgba(0, 0, 0, 0)' // Fully transparent
            }]
        };
    } catch(e) {
        // Catch errors during data preparation, e.g., if input parameters have an incorrect format.
        console.error("initializeRadarChart parameter format is not correct:", e);
    }

    try {
        // Create a new Chart.js radar chart instance
        new Chart(radarChartElement, {
            type: 'radar',
            data: radarData,
            options: {
                scales: {
                    r: { // Radial axis configuration
                        ticks: {
                            display: false, // Hide the tick labels (1, 2, 3...) on the axis
                            backdropColor: 'transparent', // Remove tick label background
                            beginAtZero: false, // Do not start the axis at 0
                            min: 1,  // Set the minimum value of the scale to 1
                            max: 7,  // Set the maximum value of the scale to 7
                            stepSize: 1,  // Set the step size to 1
                        },
                        angleLines: { display: true }, // Show lines from center to point labels
                        grid: { color: 'white' }, // Set the color of the grid lines
                        pointLabels: { // Configuration for the labels at the corners (e.g., '後旋')
                            font: { size: 16, family: "'Arial', sans-serif", weight: 'bold' },
                            color: '#FFFFFF'
                        },
                    },
                },
                plugins: {
                    legend: {
                        labels: {
                            // Configuration for the legend labels ('本次擊球', '擊球歷程')
                            color: 'white',
                            font: { size: 16, family: "'Arial', sans-serif", weight: 'bold' }
                        }
                    }
                },
            }
        });
    } catch(e) {
        console.error("initializeRadarChart create chart error:", e);
    }
}
