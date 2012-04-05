package pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem;

import static com.vaadin.terminal.Sizeable.UNITS_PIXELS;
import static com.vaadin.ui.Alignment.MIDDLE_CENTER;
import static com.vaadin.ui.Alignment.MIDDLE_LEFT;
import static pl.net.bluesoft.rnd.processtool.ui.activity.MyProcessesListPane.getDeadlineDate;
import static pl.net.bluesoft.rnd.processtool.ui.activity.MyProcessesListPane.isOutdated;
import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.labelWithIcon;
import static pl.net.bluesoft.util.lang.Formats.nvl;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.MouseEvents;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: POlszewski
 * Date: 2011-12-14
 * Time: 09:46:29
 */
@AliasName(name = "Default")
public class DefaultTaskItemProvider implements TaskItemProvider {
	@Override
	public Component getTaskPane(final TaskItemProviderParams params) {
		// ikona

		Component taskIcon = createTaskIcon(params);

		VerticalLayout til = new VerticalLayout();
		til.setHeight("100%");
		til.setWidth(60, UNITS_PIXELS);
		til.setMargin(false);
		til.addComponent(taskIcon);
		til.setComponentAlignment(taskIcon, Alignment.MIDDLE_CENTER);
		til.setExpandRatio(taskIcon, 1.0f);

		// górny pasek

		Component processIdButton = createTaskPaneProcessId(params);
		Component processDescButton = createTaskPaneProcessDesc(params);
		processDescButton.setWidth("100%");
		processIdButton.setWidth("250px"); // todo fix that ie7 shit someday

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setWidth("100%");
		hl.addComponent(processDescButton);
		hl.addComponent(processIdButton);
		hl.setExpandRatio(processDescButton, 1.0f);
		hl.setComponentAlignment(processDescButton, Alignment.MIDDLE_LEFT);
		hl.setComponentAlignment(processIdButton, Alignment.MIDDLE_RIGHT);

		// dolny pasek - opis

		Component taskDescription = createTaskPaneTaskDesc(params);

		// dolny pasek - info

		Component processInfoLayout = getTaskItemProcessInfo(params);

		GridLayout mainLayout = new GridLayout(3, 2);
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);
		mainLayout.addListener(new LayoutEvents.LayoutClickListener() {
			@Override
			public void layoutClick(LayoutEvents.LayoutClickEvent event) {
				if (event.getButton() == MouseEvents.ClickEvent.BUTTON_LEFT) {
					params.onClick();
				}
			}
		});

		mainLayout.setColumnExpandRatio(mainLayout.getColumns() - 1, 1);
		mainLayout.setRowExpandRatio(mainLayout.getRows() - 1, 1);

		mainLayout.addComponent(til, 0, 0, 0, 1);
		mainLayout.setComponentAlignment(til, Alignment.MIDDLE_CENTER);
		mainLayout.addComponent(hl, 1, 0, 2, 0);
		mainLayout.addComponent(taskDescription, 1, 1, 1, 1);
		mainLayout.setComponentAlignment(taskDescription, Alignment.MIDDLE_LEFT);
		if (processInfoLayout != null) {
			mainLayout.addComponent(processInfoLayout, 2, 1, 2, 1);
			mainLayout.setComponentAlignment(processInfoLayout, Alignment.MIDDLE_LEFT);
		}

		Panel p = new Panel();
		p.addStyleName("tti-panel");
		p.setWidth("100%");
		p.addComponent(mainLayout);
		return p;
	}

	protected Component createTaskIcon(TaskItemProviderParams params) {
		BpmTask task = params.getTask();
		final ProcessDefinitionConfig cfg = task.getProcessInstance().getDefinition();
		String path = cfg.getProcessLogo() != null ? cfg.getBpmDefinitionKey() + "_" + cfg.getId() + "_logo.png" : "/img/aperte-logo.png";
		Resource res = params.getResource(path);
		if (res == null) {
			if (cfg.getProcessLogo() != null) {
				res = params.getStreamResource(path, cfg.getProcessLogo());
			}
			else {
				res = params.getImage(path);
			}
		}
		Embedded embedded = new Embedded(null, res);
		embedded.setDescription(getProcessDescription(params));
		return embedded;
	}

	protected String getProcessDescription(TaskItemProviderParams params) {
		return params.getMessage(params.getProcessInstance().getDefinition().getDescription());
	}

	protected Component createTaskPaneProcessId(TaskItemProviderParams params) {
		ProcessInstance pi = params.getProcessInstance();
		return createOpenProcessInstanceButton(getProcessId(pi), getTaskPaneStyleName(params), params, false);
	}

	protected Component createTaskPaneProcessDesc(TaskItemProviderParams params) {
		String processDesc = getProcessDescription(params);
		return createOpenProcessInstanceButton(processDesc, getTaskPaneStyleName(params), params, true);
	}

	protected String getTaskPaneStyleName(TaskItemProviderParams params) {
		boolean running = params.getBpmSession().isProcessRunning(params.getProcessInstance().getInternalId(), params.getCtx());
		boolean outdated = running && isOutdated(new Date(), getDeadlineDate(params.getTask()));

		return "link tti-head" + (outdated ? "-outdated" : !running ? "-ended" : "");
	}

	protected Component createTaskPaneTaskDesc(TaskItemProviderParams params) {
		Label taskDescription = new Label(params.getMessage(params.getState()));
		taskDescription.setWidth(250, UNITS_PIXELS);
		taskDescription.addStyleName("tti-details");
		taskDescription.setDescription(params.getMessage("activity.state"));
		return taskDescription;
	}

	protected Component createOpenProcessInstanceButton(String caption, String styleName, final TaskItemProviderParams params, boolean showExclamation) {
		Button b = VaadinUtility.button(caption, null, styleName, new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				params.onClick();
			}
		});

		if(showExclamation){
			if(Boolean.valueOf(params.getProcessInstance().getSimpleAttributeValue("markedImportant", "false"))){
				b.setIcon(params.getImage("/img/exclamation_mark.png"));
				b.setDescription(params.getMessage("activity.task.important"));
			}
		}
		b.setHeight("26px");
		return b;
	}

	protected Component getTaskItemProcessInfo(TaskItemProviderParams params) {
		ProcessInfoBuilder builder = new ProcessInfoBuilder(params);
		builder.addComponent(createCreatorLabel(builder.getParams()), MIDDLE_CENTER);
		builder.addComponent(createCreateDateLabel(builder.getParams()), MIDDLE_CENTER);
		builder.addComponent(createAssigneeLabel(builder.getParams()), MIDDLE_CENTER);
		builder.addComponent(createDeadlineDateLabel(builder.getParams()), MIDDLE_LEFT);
		return builder.buildLayout();
	}

	protected Component createCreatorLabel(TaskItemProviderParams params) {
		String creatorName = params.getProcessInstance().getCreator().getRealName();
		return labelWithIcon(params.getImage("/img/user_creator.png"), creatorName, "tti-person", params.getMessage("activity.creator"));
	}

	protected Component createCreateDateLabel(TaskItemProviderParams params) {
		Date createDate = params.getProcessInstance().getCreateDate();
		return labelWithIcon(params.getImage("/img/date_standard.png"), formatDate(createDate), "tti-date", params.getMessage("activity.creationDate"));
	}

	protected Component createAssigneeLabel(TaskItemProviderParams params) {
		String assignedName = (params.getTask().getOwner() != null && params.getTask().getOwner().getLogin() != null && params.getTask().getOwner().getLastName() != null) ? params.getTask().getOwner().getRealName() : params.getMessage("activity.assigned.empty");
		return labelWithIcon(params.getImage("/img/user_assigned.png"), assignedName, "tti-person", params.getMessage("activity.assigned"));
	}

	protected Component createDeadlineDateLabel(TaskItemProviderParams params) {
		Date deadlineDate = getDeadlineDate(params.getTask());

		boolean running = params.getBpmSession().isProcessRunning(params.getProcessInstance().getInternalId(), params.getCtx());
		boolean outdated = running && isOutdated(new Date(), deadlineDate);

		return labelWithIcon(params.getImage("/img/date_deadline.png"),
							 deadlineDate != null ? formatDate(deadlineDate) : params.getMessage("activity.nodeadline"),
							 outdated ? "tti-date outdated" : "tti-date",
							 params.getMessage("activity.deadlineDate"));
	}

	protected String formatDate(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	///////////////////////////////////////////////////////////////////////////////

	@Override
	public Component getQueuePane(TaskItemProviderParams params) {
		final ProcessInstance pi = params.getProcessInstance();
		Panel p = new Panel(getQueuePaneHeader(params));
		VerticalLayout vl = new VerticalLayout();
		Component titleLabel = createQueuePaneTitle(params);
		HorizontalLayout hl = horizontalLayout("100%", titleLabel);
		hl.setExpandRatio(titleLabel, 1.0f);
		vl.addComponent(hl);

		Component button = createQueuePaneAssignButton(params);
		Component comment = createQueuePaneStateCommentary(params);
		vl.addComponent(horizontalLayout(comment, button));

		Component processInfo = createQueuePaneProcessInfo(params);
		if (processInfo != null) {
			vl.addComponent(processInfo);
		}

		vl.setWidth("100%");
		if (pi.getKeyword() != null) {
			vl.addComponent(createQueuePaneKeyword(params));
		}
		if (pi.getDescription() != null) {
			vl.addComponent(createQueuePaneDescription(params));
		}
		p.setWidth("100%");
		p.addComponent(vl);
		return p;
	}

	protected String getQueuePaneHeader(TaskItemProviderParams params) {
		return getProcessDescription(params) + " " +
				getProcessId(params.getProcessInstance()) + " " +
				formatDate(params.getProcessInstance().getCreateDate());
	}

	protected String getProcessId(ProcessInstance pi) {
		return nvl(pi.getExternalKey(), pi.getInternalId());
	}

	protected Component createQueuePaneTitle(TaskItemProviderParams params) {
		Label titleLabel = new Label(params.getMessage(params.getState()));
		titleLabel.addStyleName("h2 color processtool-title");
		titleLabel.setWidth("100%");
		return titleLabel;
	}

	protected Component createQueuePaneStateCommentary(TaskItemProviderParams params) {
		return new Label(nvl(params.getMessage(params.getProcessStateConfiguration().getCommentary()), ""),
		                 Label.CONTENT_XHTML);
	}

	protected Component createQueuePaneProcessInfo(TaskItemProviderParams params) {
		return null;
	}

	protected Component createQueuePaneKeyword(TaskItemProviderParams params) {
		return new Label(params.getProcessInstance().getKeyword());
	}

	protected Component createQueuePaneDescription(TaskItemProviderParams params) {
		return new Label(params.getProcessInstance().getDescription());
	}

	protected Component createQueuePaneAssignButton(final TaskItemProviderParams params) {
		return VaadinUtility.link(params.getMessage("activity.tasks.task-claim"), new Button.ClickListener() {
			@Override
			public void buttonClick(final Button.ClickEvent event) {
				params.onClick();
			}
		});
	}

	protected Component join(Component c1, Component c2) {
		if (c1 == null) {
			return c2;
		}
		if (c2 == null) {
			return c1;
		}
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(c1);
		vl.addComponent(c2);
		return vl;
	}
}
