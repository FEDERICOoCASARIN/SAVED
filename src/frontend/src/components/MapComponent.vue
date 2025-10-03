<template>
  <!-- Main container for the Leaflet map. This div serves as the target for Leaflet to render the map. -->
  <!-- It's styled to take the full width and height of its parent, with rounded corners. -->
  <div ref="mapContainer" class="w-full h-full rounded-md"></div>
</template>

<script>
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet-routing-machine";
import "leaflet-routing-machine/dist/leaflet-routing-machine.css";

// Importing an SVG for the truck icon.
import truckIconSvg from "@/components/svg/truck-fast-svgrepo-com.svg";

// Standard options for a red Leaflet marker, used as a default or for specific marker types.
const RED_MARKER_OPTIONS = {
  iconUrl:
    "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",
  shadowUrl:
    "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
};

export default {
  name: "MapComponent",
  props: {
    lat: { // Default latitude for the map center if no markers are provided.
      type: Number,
      default: 52.2159,
    },
    lng: { // Default longitude for the map center if no markers are provided.
      type: Number,
      default: 6.8906,
    },
    icon: { // Default icon type for markers ('truck', 'default', 'redIcon').
      type: String,
      default: "default",
      validator: (value) => ["truck", "default", "redIcon"].includes(value),
    },
    markers: { // An array of marker objects {lat, lng, icon, name} to display on the map.
      type: Array,
      default: () => [],
    },
    enableRouting: { // Flag to enable or disable routing between source and destination.
      type: Boolean,
      default: false,
    },
    source: { // The source waypoint for routing {lat, lng}.
      type: Object,
      default: () => null,
      validator: (value) =>
        value === null ||
        (typeof value === "object" &&
          value !== null &&
          typeof value.lat === "number" &&
          typeof value.lng === "number"),
    },
    destination: { // The destination waypoint for routing {lat, lng}.
      type: Object,
      default: () => null,
      validator: (value) =>
        value === null ||
        (typeof value === "object" &&
          value !== null &&
          typeof value.lat === "number" &&
          typeof value.lng === "number"),
    },
    zoom: { // Initial zoom level of the map.
      type: Number,
      default: 10,
    },
    zoomable: { // Enables/disables map zooming and dragging.
      type: Boolean,
      default: true,
    },
    center: { // Dynamic center of the map. When this prop changes, the map view is updated.
      type: Object,
      required: false
    },
  },

  data() {
    return {
      map: null, // Leaflet map instance
      markerLayer: null, // Layer group for managing markers
      routingControl: null, // Leaflet Routing Machine control instance
      initialCenter: null, // Stores the initial center of the map for resetting view
    };
  },

  watch: {
    /**
     * Watches for changes in the 'center' prop to dynamically update the map's view.
     * @param {object} newCenter - The new latitude and longitude for the map center.
     */
    center: {
      handler(newCenter) {
        if (this.map && newCenter && newCenter.lat !== undefined && newCenter.lng !== undefined) {
          this.map.setView([newCenter.lat, newCenter.lng], this.map.getZoom());
        }
      },
      deep: true, // Watch for nested changes in the object
      immediate: false // Do not run on component creation
    },
    /**
     * Watches for changes in the 'zoom' prop to dynamically update the map's zoom level.
     * @param {number} newZoom - The new zoom level.
     */
    zoom(newZoom) {
      if (this.map && newZoom !== undefined) {
        this.map.setZoom(newZoom);
      }
    },
    /**
     * Watches for changes in the 'markers' prop to re-render all markers on the map.
     */
    markers: {
      deep: true, // Watch for nested changes in the array elements
      handler() {
        this.renderMarkers();
      },
    },
    /**
     * Watches for changes in 'source', 'destination', or 'enableRouting' props
     * to add or remove routing lines on the map.
     */
    source: {
      deep: true,
      handler(newVal) {
        if (this.enableRouting && newVal && this.destination) {
          this.addRouting();
        } else if (!newVal && this.routingControl) {
          this.removeRouting();
        }
      },
    },
    destination: {
      deep: true,
      handler(newVal) {
        if (this.enableRouting && newVal && this.source) {
          this.addRouting();
        } else if (!newVal && this.routingControl) {
          this.removeRouting();
        }
      },
    },
    enableRouting(newVal) {
      if (newVal && this.source && this.destination) {
        this.addRouting();
      } else if (!newVal && this.routingControl) {
        this.removeRouting();
      }
    },
  },

  mounted() {
    // Determine the initial map center, prioritizing markers if available, otherwise using default props.
    const initialLat = this.markers?.[0]?.lat ?? this.lat;
    const initialLng = this.markers?.[0]?.lng ?? this.lng;

    this.initialCenter = [initialLat, initialLng];

    // Initialize the Leaflet map with specified container, center, and interactivity options.
    this.map = L.map(this.$refs.mapContainer, {
      center: this.initialCenter,
      zoom: this.zoomable, // Controls initial zoom and overall zoom level.
      dragging: this.zoomable, // Enables/disables map dragging.
      zoomControl: this.zoomable, // Shows/hides zoom controls.
      scrollWheelZoom: false, // Disables scroll wheel zoom.
      doubleClickZoom: false, // Disables double click zoom.
      boxZoom: true,
      keyboard: true,
      tap: true,
      touchZoom: true,
      attributionControl: false, // Hides the attribution text.
    });

    // Add a tile layer (OpenStreetMap) to the map.
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(this.map);

    // Render initial markers on the map.
    this.renderMarkers();

    // Add routing if enabled and source/destination are provided.
    if (this.enableRouting && this.source && this.destination) {
      this.addRouting();
    }

    // Set up a double-click event listener to reset the map view to its initial center and zoom.
    this.map.on("dblclick", () => {
      this.map.setView(this.initialCenter, this.zoom);
    });
  },

  beforeUnmount() {
    // Remove routing control if it exists to prevent memory leaks.
    this.removeRouting();
    // Destroy the map instance to clean up Leaflet resources.
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  },
  methods: {
    /**
     * Returns a Leaflet icon object based on the provided icon type.
     * @param {string} type - The type of icon to retrieve ('truck', 'redIcon', 'default').
     * @returns {L.Icon} A Leaflet Icon instance.
     */
    getIcon(type) {
      if (type === "truck") {
        return L.icon({
          iconUrl: truckIconSvg,
          iconSize: [40, 40],
          iconAnchor: [20, 40],
          popupAnchor: [0, -40],
        });
      }
      // Use RED_MARKER_OPTIONS for 'redIcon' and 'default' types.
      if (type === "redIcon" || type === "default") {
        return L.icon(RED_MARKER_OPTIONS);
      }
      // Fallback to RED_MARKER_OPTIONS for any other unknown type.
      return L.icon(RED_MARKER_OPTIONS);
    },

    /**
     * Renders markers on the map. Clears existing markers and adds new ones.
     * Adjusts map view to fit all markers if multiple are present.
     */
    renderMarkers() {
      if (!this.map) {
        console.warn("Map not initialized. Cannot render markers.");
        return;
      }

      // Clear existing markers or initialize the marker layer.
      if (this.markerLayer) {
        this.markerLayer.clearLayers();
      } else {
        this.markerLayer = L.layerGroup().addTo(this.map);
      }

      // Determine marker data: prioritize 'markers' prop, then default 'lat'/'lng', otherwise empty.
      const markerData = this.markers.length
        ? this.markers
        : this.lat != null && this.lng != null
          ? [{ lat: this.lat, lng: this.lng, icon: this.icon }]
          : [];

      // Iterate through marker data and add each marker to the map.
      markerData.forEach(({ lat, lng, icon = "default", name }) => {
        if (typeof lat !== "number" || typeof lng !== "number") {
          console.warn("Invalid marker coordinates:", { lat, lng, name });
          return;
        }

        const marker = L.marker([lat, lng], {
          icon: this.getIcon(icon),
        }).addTo(this.markerLayer);

        // Bind and manage popups for markers.
        if (name) {
          marker.bindPopup(`<b>${name}</b>`, { closeButton: false });
          // Open popup on mouseover, close on mouseout.
          marker.on("mouseover", () => marker.openPopup());
          marker.on("mouseout", () => marker.closePopup());
        } else {
          marker.bindPopup("No label available.", { closeButton: false });
        }
      });

      // Adjust map view based on the number of markers.
      if (markerData.length > 0) {
        if (markerData.length > 1) {
          // Fit map bounds to include all markers if more than one.
          const bounds = L.latLngBounds(markerData.map((m) => [m.lat, m.lng]));
          this.map.fitBounds(bounds, { padding: [50, 50] });
          this.initialCenter = bounds.getCenter(); // Update initialCenter to the new bounds center
        } else {
          // Set view to single marker if only one.
          this.map.setView([markerData[0].lat, markerData[0].lng], this.zoom);
        }
      }
    },

    /**
     * Adds routing to the map using Leaflet Routing Machine.
     * Routes between the 'source' and 'destination' props.
     * Emits 'routeCalculated' and 'routing-error' events.
     */
    addRouting() {
      if (!this.map) {
        console.warn("Map not initialized. Cannot add routing.");
        return;
      }

      // Remove existing routing control before adding a new one.
      this.removeRouting();

      console.log("Adding routing from:", this.source, "to:", this.destination);

      // Initialize Leaflet Routing Machine with waypoints and display options.
      this.routingControl = L.Routing.control({
        waypoints: [
          L.latLng(Number(this.source.lat), Number(this.source.lng)),
          L.latLng(Number(this.destination.lat), Number(this.destination.lng)),
        ],
        routeWhileDragging: false, // Do not re-route while dragging waypoints (waypoints are not draggable here anyway)
        addWaypoints: false, // Disable adding new waypoints by clicking
        draggableWaypoints: false, // Disable dragging existing waypoints
        show: false, // Hide the route instructions panel
        lineOptions: {
          styles: [{ color: "#E57229", weight: 5, opacity: 1 }], // Styling for the route line
        },
      }).addTo(this.map); // Add the routing control to the map.

      // Event listener for when routes are found.
      this.routingControl.on("routesfound", (e) => {
        const routes = e.routes;
        if (routes.length > 0) {
          const summary = routes[0].summary;
          console.log("Route found:", routes[0].summary);

          // Emit event with calculated distance and time.
          this.$emit('routeCalculated', {
            distance: (summary.totalDistance / 1000).toFixed(1), // Distance in km, 1 decimal place
            time: summary.totalTime // Time in seconds
          });
        } else {
          console.warn("No routes found between the specified points.");
        }
      });

      // Event listener for routing errors.
      this.routingControl.on("routingerror", (err) => {
        console.error("Leaflet Routing Machine Error:", err);
        this.$emit("routing-error", err); // Emit error event
        this.removeRouting(); // Remove routing on error
      });
    },

    /**
     * Removes the existing routing control from the map.
     */
    removeRouting() {
      if (this.routingControl) {
        this.map.removeControl(this.routingControl);
        this.routingControl = null;
        console.log("Routing removed.");
      }
    },
  },
};
</script>

<style>
/* Global styles for Leaflet map container to ensure it fills its parent and inherits border-radius */
.leaflet-container {
  height: 100%; /* Ensures the map fills the height of its parent */
  width: 100%; /* Ensures the map fills the width of its parent */
  border-radius: inherit; /* Inherits border-radius from the parent element for consistent styling */
}
</style>
