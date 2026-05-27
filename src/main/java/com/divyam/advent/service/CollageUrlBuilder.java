package com.divyam.advent.service;

import java.util.List;
import java.util.Objects;

/**
 * Builds an on-demand Cloudinary collage (montage) URL from a set of photo public ids.
 *
 * <p>The first photo is colorized into a solid dark canvas sized to the grid, then every
 * photo is overlaid as an equal square tile (g_north_west + x/y). No asset is stored —
 * Cloudinary renders and caches the transformation URL on first request.
 */
public final class CollageUrlBuilder {

    private static final int MAX_TILES = 9;
    private static final int TILE = 500;
    private static final String CANVAS_COLOR = "140a1e"; // matches the app's dark background

    private CollageUrlBuilder() {
    }

    public static String build(String cloudName, List<String> publicIds) {
        if (cloudName == null || cloudName.isBlank() || publicIds == null) {
            return null;
        }
        List<String> ids = publicIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .limit(MAX_TILES)
                .toList();
        if (ids.isEmpty()) {
            return null;
        }

        int count = ids.size();
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);
        int width = cols * TILE;
        int height = rows * TILE;

        StringBuilder url = new StringBuilder();
        url.append("https://res.cloudinary.com/").append(cloudName).append("/image/upload/");
        // Base: first photo turned into a solid canvas so empty trailing cells look intentional.
        url.append("c_fill,w_").append(width).append(",h_").append(height)
                .append(",e_colorize:100,co_rgb:").append(CANVAS_COLOR).append('/');

        for (int i = 0; i < count; i++) {
            int x = (i % cols) * TILE;
            int y = (i / cols) * TILE;
            String layer = ids.get(i).replace("/", ":");
            url.append("l_").append(layer)
                    .append(",w_").append(TILE).append(",h_").append(TILE).append(",c_fill")
                    .append("/fl_layer_apply,g_north_west,x_").append(x).append(",y_").append(y)
                    .append('/');
        }

        // Base public id keeps its real path (slashes), with a delivery extension.
        url.append(ids.get(0)).append(".jpg");
        return url.toString();
    }
}
