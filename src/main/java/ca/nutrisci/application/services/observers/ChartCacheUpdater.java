package ca.nutrisci.application.services.observers;

import ca.nutrisci.application.dto.ChartDTO;
import ca.nutrisci.application.dto.GroupedBarChartDTO;
import ca.nutrisci.application.dto.SwapImpactDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages caching of chart data
 */
public class ChartCacheUpdater {
    private final Map<UUID, Map<String, ChartDTO>> chartCache;
    private final Map<UUID, Map<String, GroupedBarChartDTO>> groupedChartCache;
    private final Map<UUID, Map<String, SwapImpactDTO>> swapImpactCache;

    public ChartCacheUpdater() {
        this.chartCache = new ConcurrentHashMap<>();
        this.groupedChartCache = new ConcurrentHashMap<>();
        this.swapImpactCache = new ConcurrentHashMap<>();
    }

    public void cacheChart(UUID profileId, String chartKey, ChartDTO chart) {
        chartCache.computeIfAbsent(profileId, k -> new ConcurrentHashMap<>())
                 .put(chartKey, chart);
    }

    public ChartDTO getCachedChart(UUID profileId, String chartKey) {
        return chartCache.getOrDefault(profileId, new HashMap<>())
                        .get(chartKey);
    }

    public void cacheGroupedChart(UUID profileId, String chartKey, GroupedBarChartDTO chart) {
        groupedChartCache.computeIfAbsent(profileId, k -> new ConcurrentHashMap<>())
                        .put(chartKey, chart);
    }

    public GroupedBarChartDTO getCachedGroupedChart(UUID profileId, String chartKey) {
        return groupedChartCache.getOrDefault(profileId, new HashMap<>())
                               .get(chartKey);
    }

    public void cacheSwapImpact(UUID profileId, String chartKey, SwapImpactDTO chart) {
        swapImpactCache.computeIfAbsent(profileId, k -> new ConcurrentHashMap<>())
                      .put(chartKey, chart);
    }

    public SwapImpactDTO getCachedSwapImpact(UUID profileId, String chartKey) {
        return swapImpactCache.getOrDefault(profileId, new HashMap<>())
                             .get(chartKey);
    }

    public void clearCache(UUID profileId) {
        chartCache.remove(profileId);
        groupedChartCache.remove(profileId);
        swapImpactCache.remove(profileId);
    }
} 