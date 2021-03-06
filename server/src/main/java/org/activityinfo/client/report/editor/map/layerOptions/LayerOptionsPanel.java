package org.activityinfo.client.report.editor.map.layerOptions;

import org.activityinfo.client.dispatch.Dispatcher;
import org.activityinfo.client.i18n.I18N;
import org.activityinfo.client.report.editor.map.MapResources;
import org.activityinfo.shared.command.Filter;
import org.activityinfo.shared.report.model.DimensionType;
import org.activityinfo.shared.report.model.clustering.Clustering;
import org.activityinfo.shared.report.model.layers.BubbleMapLayer;
import org.activityinfo.shared.report.model.layers.IconMapLayer;
import org.activityinfo.shared.report.model.layers.MapLayer;
import org.activityinfo.shared.report.model.layers.PiechartMapLayer;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public final class LayerOptionsPanel extends LayoutContainer implements HasValue<MapLayer> {
	
	public static final int WIDTH = 271;
	public static final int HEIGHT = 377;

	private MapLayer selectedMapLayer;

	// Options for every supported MapLayer type
	private BubbleLayerOptions bubbleMapLayerOptions = new BubbleLayerOptions();
	private IconLayerOptions iconMapLayerOptions = new IconLayerOptions();
	private PiechartLayerOptions piechartMapLayerOptions;

	// Clustering options
	private ClusteringOptionsWidget clusteringOptions;

	private ContentPanel stylePanel;
	private ContentPanel clusteringPanel;
	private LayerFilterPanel filterPanel;
	
	private Dispatcher dispatcher;

	private AccordionLayout layout;

	public LayerOptionsPanel(Dispatcher service) {
		super();
	
		this.dispatcher = service;

		initializeComponent();
		
		createBubbleLayerOptionsWidget();
		createIconLayerOptionsWidget();
		createPiechartLayerOptionsWidget();

		createClusteringOptions(service);
		createFilterPanel(service);
	}

	private void createClusteringOptions(Dispatcher service) {
		clusteringOptions = new ClusteringOptionsWidget(service);
		clusteringOptions.addValueChangeHandler(new ValueChangeHandler<Clustering>() {
			@Override
			public void onValueChange(ValueChangeEvent<Clustering> event) {
				if (selectedMapLayer != null) {
					selectedMapLayer.setClustering(event.getValue());
					ValueChangeEvent.fire(LayerOptionsPanel.this, selectedMapLayer);
				}
			}
		});
		clusteringPanel.add(clusteringOptions);
	}

	private void createPiechartLayerOptionsWidget() {
		piechartMapLayerOptions = new PiechartLayerOptions(dispatcher);
		piechartMapLayerOptions.addValueChangeHandler(new ValueChangeHandler<PiechartMapLayer>() {
			@Override
			public void onValueChange(ValueChangeEvent<PiechartMapLayer> event) {
				ValueChangeEvent.fire(LayerOptionsPanel.this, event.getValue());
			}
		});
	}

	private void createIconLayerOptionsWidget() {
		iconMapLayerOptions.addValueChangeHandler(new ValueChangeHandler<IconMapLayer>() {
			@Override
			public void onValueChange(ValueChangeEvent<IconMapLayer> event) {
				ValueChangeEvent.fire(LayerOptionsPanel.this, event.getValue());
			}
		});
	}

	private void createBubbleLayerOptionsWidget() {
		bubbleMapLayerOptions.addValueChangeHandler(new ValueChangeHandler<BubbleMapLayer>() {
			@Override
			public void onValueChange(ValueChangeEvent<BubbleMapLayer> event) {
				ValueChangeEvent.fire(LayerOptionsPanel.this, event.getValue());
			}
		});
	}

	private void initializeComponent() {
		setWidth(WIDTH);
		setHeight(HEIGHT);
		
		layout = new AccordionLayout() {

			@Override
			protected void renderComponent(Component component, int index,
					El target) {
				super.renderComponent(component, index, target);
				
				// keep the default style of rounded corners on the content panels
			    El.fly(((ContentPanel)component).getElement("header"))
			    	.removeStyleName("x-accordion-hd");
			}

			@Override
			protected void setItemSize(Component item, Size size) {
				// don't set the height of the panels
				ContentPanel cp = (ContentPanel) item;
				cp.setWidth(size.width);
			}
		};
		layout.setHideCollapseTool(true);
		setLayout(layout);
		
		//setFieldsetHeadingToLayerName();
		stylePanel = new ContentPanel();
		stylePanel.setHeading(I18N.CONSTANTS.style()); 
		stylePanel.setIcon(AbstractImagePrototype.create(MapResources.INSTANCE.styleIcon()));
		stylePanel.setCollapsible(true);
		stylePanel.setHideCollapseTool(true);
		stylePanel.setAnimCollapse(true);
		
		ToolButton closeBtn = new ToolButton("x-tool-close");
	    closeBtn.addListener(Events.Select, new Listener<ComponentEvent>() {
	        @Override
			public void handleEvent(ComponentEvent ce) {
	          fadeOut();
	        }
	      });
	    stylePanel.getHeader().addTool(closeBtn);
	    
		add(stylePanel);

		clusteringPanel = new ContentPanel();
		clusteringPanel.setIcon(AbstractImagePrototype.create(MapResources.INSTANCE.clusterIcon()));
		clusteringPanel.setHeading(I18N.CONSTANTS.clustering());
		clusteringPanel.setCollapsible(true);
		clusteringPanel.setHideCollapseTool(true);
		clusteringPanel.setAnimCollapse(true);
		
		add(clusteringPanel);
	}


	private void createFilterPanel(Dispatcher service) {
		filterPanel = new LayerFilterPanel(service);
		filterPanel.addValueChangeHandler(new ValueChangeHandler<Filter>() {
			@Override
			public void onValueChange(ValueChangeEvent<Filter> event) {
				selectedMapLayer.setFilter(event.getValue());
				ValueChangeEvent.fire(LayerOptionsPanel.this, selectedMapLayer);
			}
		});
		add(filterPanel);
	}
	
	private LayerOptionsWidget fromLayer(MapLayer mapLayer) {
		if (mapLayer instanceof BubbleMapLayer) {
			return bubbleMapLayerOptions;
		}
		if (mapLayer instanceof IconMapLayer) {
			return iconMapLayerOptions;
		}
		if (mapLayer instanceof PiechartMapLayer) {
			return piechartMapLayerOptions;
		}
		
		return null;
	}
	
	private Filter baseFilterFromLayer(MapLayer layer) {
		Filter filter = new Filter();
		filter.addRestriction(DimensionType.Indicator, layer.getIndicatorIds());
		return filter;
	}
	
	private void setStyleOptions(LayerOptionsWidget layerOptionsWidget) {
		stylePanel.removeAll();
		stylePanel.add((Widget)layerOptionsWidget);
		stylePanel.layout();
		
	}
	
	public void fadeOut() {
		el().fadeOut(new FxConfig(new Listener<FxEvent>() {
			
			@Override
			public void handleEvent(FxEvent be) {
				hide();
			}
		}));
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<MapLayer> handler) {
		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public MapLayer getValue() {
		return selectedMapLayer;
	}
	
	@Override
	public void setValue(MapLayer mapLayer) {
		if(mapLayer != selectedMapLayer) {
			this.selectedMapLayer = mapLayer;
			LayerOptionsWidget layerOptionsWidget = fromLayer(mapLayer);
			
			if (mapLayer instanceof BubbleMapLayer) {
				bubbleMapLayerOptions.setValue((BubbleMapLayer) mapLayer);
			}
			if (mapLayer instanceof IconMapLayer) {
				iconMapLayerOptions.setValue((IconMapLayer) mapLayer);
			}
			if (mapLayer instanceof PiechartMapLayer) {
				piechartMapLayerOptions.setValue((PiechartMapLayer) mapLayer);
			}
			
			setStyleOptions(layerOptionsWidget);
			clusteringOptions.loadForm(mapLayer);
			clusteringOptions.setValue(mapLayer.getClustering(), false);
	
			filterPanel.getFilterPanelSet().applyBaseFilter(baseFilterFromLayer(mapLayer));
			filterPanel.setValue(mapLayer.getFilter(), false);
		}
	}

	@Override
	public void setValue(MapLayer mapLayer, boolean fireEvents) {
		setValue(mapLayer);
		if (fireEvents) {
			ValueChangeEvent.fire(this, mapLayer);
		}
	}

	public void showStyle(MapLayer mapLayer) {
		show(mapLayer, stylePanel);
	}

	public void showAggregation(MapLayer mapLayer) {
		show(mapLayer, clusteringPanel);
	}
	

	public void showFilter(MapLayer mapLayer) {
		show(mapLayer, filterPanel);
	}
	
	private void show(MapLayer mapLayer, ContentPanel panel) {
		if(!isVisible()) {
			setVisible(true);
		}
		el().fadeIn(FxConfig.NONE);
		setValue(mapLayer);
		layout.setActiveItem(panel);
	}

	public void onLayerSelectionChanged(MapLayer model) {
		if (model == null) {
			setVisible(false);
		} else if(isVisible()) {
			setValue(model);
		}
	}
}
