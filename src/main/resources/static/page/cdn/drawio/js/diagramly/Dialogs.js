/**
 * Copyright (c) 2006-2020, JGraph Holdings Ltd
 * Copyright (c) 2006-2020, draw.io AG
 */

var StorageDialog = function(editorUi, fn, rowLimit)
{
	rowLimit = (rowLimit != null) ? rowLimit : 2;
	
	var div = document.createElement('div');
	div.style.textAlign = 'center';
	div.style.whiteSpace = 'nowrap';
	div.style.paddingTop = '0px';
	div.style.paddingBottom = '20px';
	
	var buttons = document.createElement('div');
	buttons.style.border = '1px solid #d3d3d3';
	buttons.style.borderWidth = '1px 0px 1px 0px';
	buttons.style.padding = '10px 0px 20px 0px';
	
	var count = 0, totalBtns = 0;
	var container = document.createElement('div');
	container.style.paddingTop = '2px';
	buttons.appendChild(container);
	
	var p3 = document.createElement('p');
	
	function addLogo(img, title, mode, clientName, labels, clientFn)
	{
		totalBtns++;
		
		if (++count > rowLimit)
		{
			mxUtils.br(container);
			count = 1;
		}
		
		var button = document.createElement('a');
		button.style.overflow = 'hidden';
		button.style.display = 'inline-block';
		button.className = 'geBaseButton';
		button.style.boxSizing = 'border-box';
		button.style.fontSize = '11px';
		button.style.position = 'relative';
		button.style.margin = '4px';
		button.style.marginTop = '8px';
		button.style.marginBottom = '0px';
		button.style.padding = '8px 10px 8px 10px';
		button.style.width = '88px';
		button.style.height = '100px';
		button.style.whiteSpace = 'nowrap';
		button.setAttribute('title', title);
		
		var label = document.createElement('div');
		label.style.textOverflow = 'ellipsis';
		label.style.overflow = 'hidden';
		label.style.position = 'absolute';
		label.style.bottom = '8px';
		label.style.left = '0px';
		label.style.right = '0px';
		mxUtils.write(label, title);
		button.appendChild(label);
		
		if (img != null)
		{
			var logo = document.createElement('img');
			logo.setAttribute('src', img);
			logo.setAttribute('border', '0');
			logo.setAttribute('align', 'absmiddle');
			logo.style.width = '60px';
			logo.style.height = '60px';
			logo.style.paddingBottom = '6px';

			button.appendChild(logo);
		}
		else
		{
			label.style.paddingTop = '5px';
			label.style.whiteSpace = 'normal';
			
			// Handles special case
			if (mxClient.IS_IOS)
			{
				button.style.padding = '0px 10px 20px 10px';
				button.style.top = '6px';
			}
			else if (mxClient.IS_FF)
			{
				label.style.paddingTop = '0px';
				label.style.marginTop = '-2px';
			}
		}
		
		if (labels != null)
		{
			for (var i = 0; i < labels.length; i++)
			{
				mxUtils.br(label);
				mxUtils.write(label, labels[i]);
			}
		}
		
		function initButton()
		{
			mxEvent.addListener(button, 'click', (clientFn != null) ? clientFn : function()
			{
				if (mode == App.MODE_GOOGLE && editorUi.spinner.spin(document.body, mxResources.get('authorizing')))
				{
					// Tries immediate authentication
					editorUi.drive.checkToken(mxUtils.bind(this, function()
					{
						editorUi.spinner.stop();
						editorUi.setMode(mode, true);
						fn();
					}));
				}
				else if (mode == App.MODE_ONEDRIVE && editorUi.spinner.spin(document.body, mxResources.get('authorizing')))
				{
					// Tries immediate authentication
					editorUi.oneDrive.checkToken(mxUtils.bind(this, function()
					{
						editorUi.spinner.stop();
						editorUi.setMode(mode, true);
						fn();
					}), function(err)
					{
						editorUi.spinner.stop();
						editorUi.handleError(err);
					});
				}
				else
				{
					editorUi.setMode(mode, true);
					fn();
				}
			});
		};
		
		// Supports lazy loading
		if (clientName != null && editorUi[clientName] == null)
		{
			logo.style.visibility = 'hidden';
			mxUtils.setOpacity(label, 10);
			var size = 12;
			
			var spinner = new Spinner({
				lines: 12, // The number of lines to draw
				length: size, // The length of each line
				width: 5, // The line thickness
				radius: 10, // The radius of the inner circle
				rotate: 0, // The rotation offset
				color: 'light-dark(#000000, #C0C0C0)',
				speed: 1.5, // Rounds per second
				trail: 60, // Afterglow percentage
				shadow: false, // Whether to render a shadow
				hwaccel: false, // Whether to use hardware acceleration
				top: '40%',
				zIndex: 2e9 // The z-index (defaults to 2000000000)
			});
			spinner.spin(button);
			
			// Timeout after 30 secs
			var timeout = window.setTimeout(function()
			{
				if (editorUi[clientName] == null)
				{
					spinner.stop();
					button.style.display = 'none';
				}
			}, 30000);
			
			editorUi.addListener('clientLoaded', mxUtils.bind(this, function(sender, evt)
			{
				if (editorUi[clientName] != null && evt.getProperty('client') == editorUi[clientName])
				{
					window.clearTimeout(timeout);
					mxUtils.setOpacity(label, 100);
					logo.style.visibility = '';
					spinner.stop();
					initButton();
					
					if (clientName == 'drive' && p3.parentNode != null)
					{
						p3.parentNode.removeChild(p3);
					}
				}
			}));
		}
		else
		{
			initButton();
		}

		container.appendChild(button);
	};

	var hd = document.createElement('p');
	hd.style.cssText = 'font-size:22px;padding:4px 0 16px 0;margin:0;color:gray;';
	mxUtils.write(hd, mxResources.get('saveDiagramsTo') + ':');
	div.appendChild(hd);
	
	var addButtons = function()
	{
		count = 0;
		
		if (typeof window.DriveClient === 'function')
		{
			addLogo(IMAGE_PATH + '/google-drive-logo.svg', mxResources.get('googleDrive'), App.MODE_GOOGLE, 'drive');
		}

		if (editorUi.m365 != null)
		{
			addLogo(IMAGE_PATH + '/onedrive-logo.svg', mxResources.get('m365'), App.MODE_M365, 'm365');
		}

		if (urlParams['noDevice'] != '1')
		{
			addLogo(IMAGE_PATH + '/osa_drive-harddisk.png', mxResources.get('device'), App.MODE_DEVICE);
		}

		if (isLocalStorage && (urlParams['browser'] == '1' || urlParams['offline'] == '1'))
		{
			addLogo(IMAGE_PATH + '/osa_database.png', mxResources.get('browser'), App.MODE_BROWSER);
		}

		if (typeof window.OneDriveClient === 'function')
		{
			addLogo(IMAGE_PATH + '/onedrive-logo.svg', mxResources.get('oneDrive'), App.MODE_ONEDRIVE, 'oneDrive');
		}

		if (editorUi.gitHub != null)
		{
			addLogo(IMAGE_PATH + '/github-logo.svg', mxResources.get('github'), App.MODE_GITHUB, 'gitHub');
		}

		if (editorUi.gitLab != null)
		{
			addLogo(IMAGE_PATH + '/gitlab-logo.svg', mxResources.get('gitlab'), App.MODE_GITLAB, 'gitLab');
		}
	};
	
	div.appendChild(buttons);
	addButtons();

	var later = document.createElement('span');
	later.style.position = 'absolute';
	later.style.cursor = 'pointer';
	later.style.bottom = '27px';
	later.style.color = 'gray';
	later.style.userSelect = 'none';
	later.style.textAlign = 'center';
	later.style.left = '50%';
	mxUtils.setPrefixedStyle(later.style, 'transform', 'translate(-50%,0)');
	mxUtils.write(later, mxResources.get('decideLater'));
	div.appendChild(later);

	mxEvent.addListener(later, 'click', function()
	{
		editorUi.hideDialog();
		var prev = Editor.useLocalStorage;
		editorUi.createFile(editorUi.defaultFilename,
			null, null, null, null, null, null, true);
		Editor.useLocalStorage = prev;
	});
	
	// Checks if Google Drive is missing after a 5 sec delay
	if (mxClient.IS_SVG && isLocalStorage && urlParams['gapi'] != '0' &&
		(document.documentMode == null || document.documentMode >= 10))
	{
		window.setTimeout(function()
		{
			if (editorUi.drive == null)
			{
				// To check for Disconnect plugin in chrome use mxClient.IS_GC and check for URL:
				// chrome-extension://jeoacafpbcihiomhlakheieifhpjdfeo/scripts/vendor/jquery/jquery-2.0.3.min.map
				p3.style.padding = '7px';
				p3.style.fontSize = '9pt';
				p3.style.marginTop = '-14px';
				p3.innerHTML = '<a style="background-color:#dcdcdc;padding:6px;color:black;text-decoration:none;" ' +
					'href="https://www.drawio.com/doc/faq/google-drive-connection-problems" target="_blank">' +
					'<img border="0" src="' + mxGraph.prototype.warningImage.src + '" align="absmiddle" ' +
					'style="margin-top:-4px"> ' + mxResources.get('googleDriveMissingClickHere') + '</a>';
				div.appendChild(p3);
			}
		}, 5000);
	}
	
	this.container = div;
};

/**
 * Constructs a dialog for creating new files from templates.
 */
var SplashDialog = function(editorUi)
{
	var div = document.createElement('div');
	div.style.textAlign = 'center';
	
	if (mxClient.IS_CHROMEAPP || EditorUi.isElectronApp)
	{
		var elt = editorUi.addLanguageMenu(div, false, '28px');
		
		if (elt != null)
		{
			elt.style.bottom = '24px';
		}
	}
	
	var logo = document.createElement('img');
	logo.setAttribute('border', '0');
	logo.setAttribute('align', 'absmiddle');
	logo.style.width = '32px';
	logo.style.height = '32px';
	logo.style.marginRight = '8px';
	logo.style.marginTop = '-4px';
	
	var buttons = document.createElement('div');
	buttons.style.margin = '8px 0px 0px 0px';
	buttons.style.padding = '18px 0px 24px 0px';
	
	var service = '';
	
	if (editorUi.mode == App.MODE_GOOGLE)
	{
		logo.src = IMAGE_PATH + '/google-drive-logo.svg';
		service = mxResources.get('googleDrive');
	}
	else if (editorUi.mode == App.MODE_DROPBOX)
	{
		logo.src = IMAGE_PATH + '/dropbox-logo.svg';
		service = mxResources.get('dropbox');
	}
	else if (editorUi.mode == App.MODE_ONEDRIVE)
	{
		logo.src = IMAGE_PATH + '/onedrive-logo.svg';
		service = mxResources.get('oneDrive');
	}
	else if (editorUi.mode == App.MODE_M365)
	{
		logo.src = IMAGE_PATH + '/onedrive-logo.svg';
		service = mxResources.get('m365');
	}
	else if (editorUi.mode == App.MODE_GITHUB)
	{
		logo.src = IMAGE_PATH + '/github-logo.svg';
		service = mxResources.get('github');
	}
	else if (editorUi.mode == App.MODE_GITLAB)
	{
		logo.src = IMAGE_PATH + '/gitlab-logo.svg';
		service = mxResources.get('gitlab');
	}
	else if (editorUi.mode == App.MODE_BROWSER)
	{
		logo.src = IMAGE_PATH + '/osa_database.png';
		service = mxResources.get('browser');
	}
	else if (editorUi.mode == App.MODE_TRELLO)
	{
		logo.src = IMAGE_PATH + '/trello-logo.svg';
		service = mxResources.get('trello');
	}
	else
	{
		logo.src = IMAGE_PATH + '/osa_drive-harddisk.png';
		buttons.style.paddingBottom = '10px';
		buttons.style.paddingTop = '30px';
		service = mxResources.get('device');
	}

	var btn = document.createElement('button');
	btn.className = 'geBigButton';
	btn.style.marginBottom = '8px';
	btn.style.fontSize = '18px';
	btn.style.padding = '10px';
	btn.style.width = '340px';
	
	if (!mxClient.IS_CHROMEAPP && !EditorUi.isElectronApp)
	{
		buttons.style.border = '1px solid #d3d3d3';
		buttons.style.borderWidth = '1px 0px 1px 0px';
	
		var table = document.createElement('table');
		var tbody = document.createElement('tbody');
		var row = document.createElement('tr');
		var left = document.createElement('td');
		var right = document.createElement('td');
		table.setAttribute('align', 'center');
		left.appendChild(logo);
		
		var title = document.createElement('div');
		title.style.fontSize = '22px';
		title.style.paddingBottom = '6px';
		title.style.color = 'gray';
		mxUtils.write(title, service);
		
		right.style.textAlign = 'left';
		right.appendChild(title);
		
		row.appendChild(left);
		row.appendChild(right);
		tbody.appendChild(row);
		table.appendChild(tbody);
		div.appendChild(table);
	
		var change = document.createElement('span');
		change.style.cssText = 'position:absolute;cursor:pointer;bottom:27px;color:gray;userSelect:none;text-align:center;left:50%;';
		mxUtils.setPrefixedStyle(change.style, 'transform', 'translate(-50%,0)');
		mxUtils.write(change, mxResources.get('changeStorage'));
		
		mxEvent.addListener(change, 'click', function()
		{
			editorUi.hideDialog(false);
			editorUi.setMode(null);
			editorUi.clearMode();
			editorUi.showSplash(true);
		});
		
		div.appendChild(change);
	}
	else
	{
		buttons.style.padding = '42px 0px 10px 0px';
		btn.style.marginBottom = '12px';
	}

	mxUtils.write(btn, mxResources.get('createNewDiagram'));
	
	mxEvent.addListener(btn, 'click', function()
	{
		editorUi.hideDialog();
		editorUi.actions.get('new').funct();
	});
	
	buttons.appendChild(btn);
	mxUtils.br(buttons);
	
	var btn = document.createElement('button');
	btn.className = 'geBigButton';
	btn.style.marginBottom = '22px';
	btn.style.fontSize = '18px';
	btn.style.padding = '10px';
	btn.style.width = '340px';
	
	mxUtils.write(btn, mxResources.get('openExistingDiagram'));
	
	mxEvent.addListener(btn, 'click', function()
	{
		editorUi.actions.get('open').funct();
	});
	
	buttons.appendChild(btn);

	var storage = 'undefined';
	
	if (editorUi.mode == App.MODE_GOOGLE)
	{
		storage = mxResources.get('googleDrive');
	}
	else if (editorUi.mode == App.MODE_DROPBOX)
	{
		storage = mxResources.get('dropbox');
	}
	else if (editorUi.mode == App.MODE_ONEDRIVE)
	{
		storage = mxResources.get('oneDrive');
	}
	else if (editorUi.mode == App.MODE_GITHUB)
	{
		storage = mxResources.get('github');
	}
	else if (editorUi.mode == App.MODE_GITLAB)
	{
		storage = mxResources.get('gitlab');
	}
	else if (editorUi.mode == App.MODE_TRELLO)
	{
		storage = mxResources.get('trello');
	}
	else if (editorUi.mode == App.MODE_DEVICE)
	{
		storage = mxResources.get('device');
	}
	else if (editorUi.mode == App.MODE_BROWSER)
	{
		storage = mxResources.get('browser');
	}
	
	if (!mxClient.IS_CHROMEAPP && !EditorUi.isElectronApp)
	{
		function addLogout(logout)
		{
			btn.style.marginBottom = '24px';
			
			var link = document.createElement('a');
			link.style.display = 'inline-block';
			link.style.color = 'gray';
			link.style.cursor = 'pointer';
			link.style.marginTop = '6px';
			mxUtils.write(link, mxResources.get('signOut'));

			// Makes room after last big buttons
			btn.style.marginBottom = '16px';
			buttons.style.paddingBottom = '18px';
			
			mxEvent.addListener(link, 'click', function()
			{
				editorUi.confirm(mxResources.get('areYouSure'), function()
				{
					logout();
				});
			});
			
			buttons.appendChild(link);
		};
				
		if (editorUi.mode == App.MODE_GOOGLE && editorUi.drive != null)
		{
			var driveUsers = editorUi.drive.getUsersList();
		
			if (driveUsers.length > 0)
			{
				var title = document.createElement('span');
				title.style.marginTop = '6px';
				mxUtils.write(title, mxResources.get('changeUser') + ':');
	
				// Makes room after last big buttons
				btn.style.marginBottom = '16px';
				buttons.style.paddingBottom = '18px';
				buttons.appendChild(title);
				
				var usersSelect = document.createElement('select');
				usersSelect.style.marginLeft = '4px';
				usersSelect.style.width = '140px';
				
				for (var i = 0; i < driveUsers.length; i++)
				{
					var option = document.createElement('option');
					mxUtils.write(option, driveUsers[i].displayName);
					option.value = i;
					usersSelect.appendChild(option);
					//More info (email) about the user in a disabled option
					option = document.createElement('option');
					option.innerHTML = '&nbsp;&nbsp;&nbsp;';
					mxUtils.write(option, '<' + driveUsers[i].email + '>');
					option.setAttribute('disabled', 'disabled');
					usersSelect.appendChild(option);
				}
				
				//Add account option
				var option = document.createElement('option');
				mxUtils.write(option, mxResources.get('addAccount'));
				option.value = driveUsers.length;
				usersSelect.appendChild(option);
				
				mxEvent.addListener(usersSelect, 'change', function()
				{
					var userIndex = usersSelect.value;
					var existingAccount = driveUsers.length != userIndex;
					
					if (existingAccount)
					{
						editorUi.drive.setUser(driveUsers[userIndex]);
					}
					
					editorUi.drive.authorize(existingAccount, function()
					{
						editorUi.setMode(App.MODE_GOOGLE);
						editorUi.hideDialog();
						editorUi.showSplash();
					}, function(resp)
					{
						editorUi.handleError(resp, null, function()
						{
							editorUi.hideDialog();
							editorUi.showSplash();
						});
					}, true);
				});
				
				buttons.appendChild(usersSelect);
			}
			else
			{
				addLogout(function()
				{
					editorUi.drive.logout();
				});
			}
		}
		else if (editorUi.mode == App.MODE_ONEDRIVE && editorUi.oneDrive != null && !editorUi.oneDrive.noLogout)
		{
			addLogout(function()
			{
				editorUi.oneDrive.logout();
			});
		}
		else if (editorUi.mode == App.MODE_GITHUB && editorUi.gitHub != null)
		{
			addLogout(function()
			{
				editorUi.gitHub.logout();
				editorUi.openLink('https://www.github.com/logout');
			});
		}
		else if (editorUi.mode == App.MODE_GITLAB && editorUi.gitLab != null)
		{
			addLogout(function()
			{
				editorUi.gitLab.logout();

				// Must use POST request to sign out of GitLab
				// see https://gitlab.com/gitlab-org/gitlab/-/issues/202291
				var form = document.createElement('form');
				form.setAttribute('method', 'post');
				form.setAttribute('action', DRAWIO_GITLAB_URL + '/users/sign_out');
				form.setAttribute('target', '_blank');

				document.body.appendChild(form);
				form.submit();
				form.parentNode.removeChild(form);
			});
		}
		else if (editorUi.mode == App.MODE_TRELLO && editorUi.trello != null)
		{
			if (editorUi.trello.isAuthorized())
			{
				addLogout(function()
				{
					editorUi.trello.logout();
				});
			}
		}
		else if (editorUi.mode == App.MODE_DROPBOX && editorUi.dropbox != null)
		{
			// NOTE: Dropbox has a logout option in the picker
			addLogout(function()
			{
				editorUi.dropbox.logout();
				editorUi.openLink('https://www.dropbox.com/logout');
			});
		}
	}

	div.appendChild(buttons);
	this.container = div;
};

/**
 * Constructs a new embed dialog
 */
var EmbedDialog = function(editorUi, result, timeout, ignoreSize, previewFn, title, tweet, previewTitle, filename)
{
	tweet = (tweet != null) ? tweet : 'Check out the diagram I made using @drawio';
	var div = document.createElement('div');
	div.style.height = '100%';
	div.style.display = 'flex';
	div.style.flexDirection = 'column';
	var maxSize = 500000;

	// Checks if result is a link
	var validUrl = /^https?:\/\//.test(result) || /^mailto:\/\//.test(result);

	var header = document.createElement('div');
	header.style.flexShrink = '0';
	header.style.position = 'relative';

	if (title != null)
	{
		mxUtils.write(header, title);
	}
	else
	{
		mxUtils.write(header, mxResources.get((result.length < maxSize) ?
			((validUrl) ? 'link' : 'mainEmbedNotice') : 'preview') + ':');
	}

	var size = document.createElement('span');
	size.style.cssFloat = 'right';
	size.style.color = 'gray';
	mxUtils.write(size, editorUi.formatFileSize(result.length));
	header.appendChild(size);

	div.appendChild(header);

	// Using DIV for faster rendering
	var text = document.createElement('textarea');
	text.setAttribute('autocomplete', 'off');
	text.setAttribute('autocorrect', 'off');
	text.setAttribute('autocapitalize', 'off');
	text.setAttribute('spellcheck', 'false');
	text.style.fontFamily = 'monospace';
	text.style.wordBreak = 'break-all';
	text.style.marginTop = '10px';
	text.style.resize = 'none';
	text.style.flex = '1';
	text.style.width = '100%';
	text.style.boxSizing = 'border-box';
	text.value = mxResources.get('updatingDocument');
	div.appendChild(text);

	this.init = function()
	{
		window.setTimeout(function()
		{
			if (result.length < maxSize)
			{
				text.value = result;
				text.focus();
					
				if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
				{
					text.select();
				}
				else
				{
					document.execCommand('selectAll', false, null);
				}
			}
			else
			{
				text.setAttribute('readonly', 'true');
				text.value = mxResources.get('tooLargeUseDownload');
			}
		}, 0);
	};
	
	var buttons = document.createElement('div');
	buttons.style.flexShrink = '0';
	buttons.style.textAlign = 'right';
	buttons.style.paddingTop = '14px';
	buttons.style.whiteSpace = 'nowrap';
	
	var previewBtn = null;
	
	if (EmbedDialog.showPreviewOption && !mxIsElectron &&
		!navigator.standalone && validUrl)
	{
		previewBtn = mxUtils.button((previewTitle != null) ? previewTitle :
			mxResources.get((result.length < maxSize) ? 'preview' : 'openInNewWindow'), function()
		{
			var value = (result.length < maxSize) ? text.value : result;
			
			if (previewFn != null)
			{
				previewFn(value);
			}
			else
			{
				if (validUrl)
				{
					try
					{
						var win = editorUi.openLink(value);
						
						if (win != null && (timeout == null || timeout > 0))
						{
							window.setTimeout(mxUtils.bind(this, function()
							{
								try
								{
									if (win != null && win.location.href != null &&
										win.location.href.substring(0, 8) != value.substring(0, 8))
									{
										win.close();
										editorUi.handleError({message: mxResources.get('drawingTooLarge')});
									}
								}
								catch (e)
								{
									// ignore
								}
							}), timeout || 500);
						}
					}
					catch (e)
					{
						editorUi.handleError({message: e.message || mxResources.get('drawingTooLarge')});
					}
				}
				else
				{
					var wnd = window.open();
					var doc = (wnd != null) ? wnd.document : null;
					
					if (doc != null)
					{
						doc.writeln('<html><head><title>' +
							mxUtils.htmlEntities(mxResources.get('preview')) +
							'</title><meta charset="utf-8"></head><body>' +
							(result.substring(0, 7) == '<iframe' ? result :
								mxUtils.htmlEntities(result)) + '</body></html>');
						doc.close();
					}
					else
					{
						editorUi.handleError({message: mxResources.get('errorUpdatingPreview')});
					}
				}
			}
		});
		
		previewBtn.className = 'geBtn';
		buttons.appendChild(previewBtn);
	}
	
	var downloadBtn = mxUtils.button(mxResources.get('export'), function()
		{
			editorUi.hideDialog();
			editorUi.saveData((filename != null) ? filename : 'embed.txt', 'txt', result, 'text/plain');
		});
		
		downloadBtn.className = 'geBtn';
		buttons.appendChild(downloadBtn);

	if (!editorUi.isOffline() && result.length < maxSize)
	{
		var emailBtn = mxUtils.button('', function()
		{
			try
			{
				var url = 'mailto:?subject=' +
					encodeURIComponent(filename || editorUi.defaultFilename) + '&body=' +
					encodeURIComponent(text.value);

				editorUi.openLink(url);
			}
			catch (e)
			{
				editorUi.handleError({message: e.message || mxResources.get('drawingTooLarge')});
			}
		});
		
		var img = document.createElement('img');
		img.className = 'geAdaptiveAsset';
		img.setAttribute('src', Editor.mailImage);
		img.setAttribute('width', '18');
		img.setAttribute('height', '18');
		img.setAttribute('border', '0');
		img.style.marginBottom = '5px'

		emailBtn.appendChild(img);
		emailBtn.style.verticalAlign = 'bottom';
		emailBtn.style.paddingTop = '4px';
		emailBtn.style.minWidth = '46px'
		emailBtn.className = 'geBtn';
		buttons.appendChild(emailBtn);
	}

	var closeBtn = mxUtils.button(mxResources.get('close'), function()
	{
		editorUi.hideDialog();
	});

	buttons.appendChild(closeBtn);

	var copyBtn = mxUtils.button(mxResources.get('copy'), function()
	{
		text.focus();
		
		if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
		{
			text.select();
		}
		else
		{
			document.execCommand('selectAll', false, null);
		}
		
		document.execCommand('copy');
		editorUi.alert(mxResources.get('copiedToClipboard'));
	});

	if (result.length < maxSize)
	{
		// Does not work in Safari and shows annoying dialog for IE11-
		if (!mxClient.IS_SF && document.documentMode == null)
		{
			buttons.appendChild(copyBtn);
			copyBtn.className = 'geBtn gePrimaryBtn';
			closeBtn.className = 'geBtn';
		}
		else
		{
			closeBtn.className = 'geBtn gePrimaryBtn';
		}
	}
	else if (previewBtn != null)
	{
		buttons.appendChild(previewBtn);
		closeBtn.className = 'geBtn';
		previewBtn.className = 'geBtn gePrimaryBtn';
	}
	
	div.appendChild(buttons);
	this.container = div;
};

/**
 * Add embed dialog option.
 */
EmbedDialog.showPreviewOption = true;

/**
 * Constructs a new parse dialog.
 */
var CreateGraphDialog = function(editorUi, title, type)
{
	var div = document.createElement('div');
	div.style.textAlign = 'right';
	
	this.init = function()
	{
		var container = document.createElement('div');
		container.style.position = 'relative';
		container.style.border = '1px solid gray';
		container.style.boxSizing = 'border-box';
		container.style.width = '100%';
		container.style.height = '360px';
		container.style.overflow = 'hidden';
		container.style.marginBottom = '16px';
		mxEvent.disableContextMenu(container);
		div.appendChild(container);
	
		var graph = new Graph(container);
		
		graph.setCellsCloneable(true);
		graph.setPanning(true);
		graph.setAllowDanglingEdges(false);
		graph.connectionHandler.select = false;
		graph.view.setTranslate(20, 20);
		graph.border = 20;
		graph.panningHandler.useLeftButtonForPanning = true;

		// Fixes in-place editor position
	    if (mxClient.IS_SVG && graph.view.getDrawPane() != null)
		{
			var root = graph.view.getDrawPane().ownerSVGElement;
			
			if (root != null)
			{
				root.style.position = 'absolute';
			}
		}
		
		var vertexStyle = 'rounded=1;';
		var edgeStyle = 'curved=1;';
		var startStyle = 'ellipse';
		
		// FIXME: Does not work in iPad
		graph.cellRenderer.installCellOverlayListeners = function(state, overlay, shape)
		{
			mxCellRenderer.prototype.installCellOverlayListeners.apply(this, arguments);
			
			mxEvent.addListener(shape.node, (mxClient.IS_POINTER) ? 'pointerdown' : 'mousedown', function (evt)
			{
				overlay.fireEvent(new mxEventObject('pointerdown', 'event', evt, 'state', state));
			});
			
			if (!mxClient.IS_POINTER && mxClient.IS_TOUCH)
			{
				mxEvent.addListener(shape.node, 'touchstart', function (evt)
				{
					overlay.fireEvent(new mxEventObject('pointerdown', 'event', evt, 'state', state));
				});
			}
		};

		graph.getAllConnectionConstraints = function()
		{
			return null;
		};

		// Keeps highlight behind overlays
		graph.connectionHandler.marker.highlight.keepOnTop = false;
	
		graph.connectionHandler.createEdgeState = function(me)
		{
			var edge = graph.createEdge(null, null, null, null, null, edgeStyle);

			return new mxCellState(this.graph.view, edge, this.graph.getCellStyle(edge));
		};
	
		// Gets the default parent for inserting new cells. This
		// is normally the first child of the root (ie. layer 0).
		var parent = graph.getDefaultParent();
		
		var addOverlay = mxUtils.bind(this, function(cell)
		{
			// Creates a new overlay with an image and a tooltip
			var overlay = new mxCellOverlay(this.connectImage, 'Add outgoing');
			overlay.cursor = 'hand';
	
			// Installs a handler for clicks on the overlay							
			overlay.addListener(mxEvent.CLICK, function(sender, evt2)
			{
				// TODO: Add menu for picking next shape
				graph.connectionHandler.reset();
				graph.clearSelection();
				var geo = graph.getCellGeometry(cell);
				
				var v2;
				
				executeLayout(function()
				{
					v2 = graph.insertVertex(parent, null, 'Entry', geo.x, geo.y, 80, 30, vertexStyle);
					addOverlay(v2);
					graph.view.refresh(v2);
					graph.insertEdge(parent, null, '', cell, v2, edgeStyle);
				}, function()
				{
					graph.scrollCellToVisible(v2);
				});
			});
			
			// FIXME: Does not work in iPad (inserts loop)
			overlay.addListener('pointerdown', function(sender, eo)
			{
				var evt2 = eo.getProperty('event');
				var state = eo.getProperty('state');
				
				graph.popupMenuHandler.hideMenu();
				graph.stopEditing(false);
				
				var pt = mxUtils.convertPoint(graph.container,
						mxEvent.getClientX(evt2), mxEvent.getClientY(evt2));
				graph.connectionHandler.start(state, pt.x, pt.y);
				graph.isMouseDown = true;
				graph.isMouseTrigger = mxEvent.isMouseEvent(evt2);
				mxEvent.consume(evt2);
			});
			
			// Sets the overlay for the cell in the graph
			graph.addCellOverlay(cell, overlay);
		});
						
		// Adds cells to the model in a single step
		graph.getModel().beginUpdate();
		var v1;
		try
		{
			v1 = graph.insertVertex(parent, null, 'Start', 0, 0, 80, 30, startStyle);
			addOverlay(v1);
		}
		finally
		{
			// Updates the display
			graph.getModel().endUpdate();
		}
	
		var layout;
		
		if (type == 'horizontalTree')
		{
			layout = new mxCompactTreeLayout(graph);
			layout.edgeRouting = false;
			layout.levelDistance = 30;
			layout.sortEdges = true;
			edgeStyle = 'edgeStyle=elbowEdgeStyle;elbow=horizontal;';
		}
		else if (type == 'verticalTree')
		{
			layout = new mxCompactTreeLayout(graph, false);
			layout.edgeRouting = false;
			layout.levelDistance = 30;
			layout.sortEdges = true;
			edgeStyle = 'edgeStyle=elbowEdgeStyle;elbow=vertical;';
		}
		else if (type == 'radialTree')
		{
			layout = new mxRadialTreeLayout(graph, false);
			layout.edgeRouting = false;
			layout.levelDistance = 80;
		}
		else if (type == 'verticalFlow')
		{
			layout = new mxHierarchicalLayout(graph, mxConstants.DIRECTION_NORTH);
		}
		else if (type == 'horizontalFlow')
		{
			layout = new mxHierarchicalLayout(graph, mxConstants.DIRECTION_WEST);
		}
		else if (type == 'circle')
		{
			layout = new mxCircleLayout(graph);
		}
		else
		{
			layout = new mxFastOrganicLayout(graph, false);
			layout.forceConstant = 80;
		}
		
		if (layout != null)
		{
			var executeLayout = function(change, post)
			{
				graph.getModel().beginUpdate();
				try
				{
					if (change != null)
					{
						change();
					}
					
					layout.execute(graph.getDefaultParent(), v1);
				}
				catch (e)
				{
					throw e;
				}
				finally
				{
					// New API for animating graph layout results asynchronously
					var morph = new mxMorphing(graph);
					morph.addListener(mxEvent.DONE, mxUtils.bind(this, function()
					{
						graph.getModel().endUpdate();
						
						if (post != null)
						{
							post();
						}
					}));
					
					morph.startAnimation();
				}
			};
			
			var edgeHandleConnect = mxEdgeHandler.prototype.connect;
			mxEdgeHandler.prototype.connect = function(edge, terminal, isSource, isClone, me)
			{
				edgeHandleConnect.apply(this, arguments);
				executeLayout();
			};
			
			graph.resizeCell = function()
			{
				mxGraph.prototype.resizeCell.apply(this, arguments);
		
				executeLayout();
			};
		
			graph.connectionHandler.addListener(mxEvent.CONNECT, function()
			{
				executeLayout();
			});
		}

		var cancelBtn = mxUtils.button(mxResources.get('close'), function()
		{
			editorUi.confirm(mxResources.get('areYouSure'), function()
			{
				editorUi.hideDialog();
			});
		})
		
		cancelBtn.className = 'geBtn';
		
		if (editorUi.editor.cancelFirst)
		{
			div.appendChild(cancelBtn);
		}
		
		var okBtn = mxUtils.button(mxResources.get('insert'), function(evt)
		{
			graph.clearCellOverlays();
			
			var cells = graph.getModel().getChildren(graph.getDefaultParent());
			var pt = (mxEvent.isAltDown(evt)) ?
				editorUi.editor.graph.getFreeInsertPoint() :
				editorUi.editor.graph.getCenterInsertPoint(
				graph.getBoundingBoxFromGeometry(cells, true));
			cells = editorUi.editor.graph.importCells(cells, pt.x, pt.y);
			var view = editorUi.editor.graph.view;
			var temp = view.getBounds(cells);

			if (temp != null)
			{
				temp.x -= view.translate.x;
				temp.y -= view.translate.y;
				editorUi.editor.graph.scrollRectToVisible(temp);
				editorUi.editor.graph.setSelectionCells(cells);
			}
			
			editorUi.hideDialog();
		});
		
		div.appendChild(okBtn);
		okBtn.className = 'geBtn gePrimaryBtn';
		
		if (!editorUi.editor.cancelFirst)
		{
			div.appendChild(cancelBtn);
		}

		this.graph = graph;
	};

	this.container = div;
};

/**
 * 
 */
CreateGraphDialog.prototype.connectImage = new mxImage('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAaCAYAAACpSkzOAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBNYWNpbnRvc2giIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RjQ3OTk0QjMyRDcyMTFFNThGQThGNDVBMjNBMjFDMzkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RjQ3OTk0QjQyRDcyMTFFNThGQThGNDVBMjNBMjFDMzkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDoyRjA0N0I2MjJENzExMUU1OEZBOEY0NUEyM0EyMUMzOSIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpGNDc5OTRCMjJENzIxMUU1OEZBOEY0NUEyM0EyMUMzOSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PjIf+MgAAATlSURBVHjanFZraFxFFD735u4ru3ls0yZG26ShgmJoKK1J2vhIYzBgRdtIURHyw1hQUH9IxIgI2h8iCEUF/1RRlNQYCsYfCTHVhiTtNolpZCEStqSC22xIsrs1bDfu7t37Gs/cO3Ozxs1DBw73zpk555vzmHNGgJ0NYatFgmNLYUHYUoHASMz5ijmgVLmxgfKCUiBxC4ACJAeSG8nb1dVVOTc3dyoSibwWDofPBIPBJzo7O8vpGtvjpDICGztxkciECpF2LS0tvZtOpwNkk5FKpcYXFxffwL1+JuPgllPj8nk1F6RoaGjoKCqZ5ApljZDZO4SMRA0SuG2QUJIQRV8HxMOM9vf3H0ZZH9Nhg20MMl2QkFwjIyNHWlpahtADnuUMwLcRHX5aNSBjCJYEsSSLUeLEbhGe3ytCmQtA1/XY+Pj46dbW1iDuyCJp9BC5ycBj4hoeHq5ra2sbw0Xn1ZgBZ+dVkA1Lc+6p0Ck2p0QS4Ox9EhwpEylYcmBg4LH29vYQLilIOt0u5FhDfevNZDI/u93uw6PLOrwTUtjxrbPYbhD42WgMrF8JmR894ICmCgnQjVe8Xu8pXEkzMJKbuo5oNPomBbm1ZsD7s2kwFA1JZ6QBUXWT1nmGNc/qoMgavDcrQzxjQGFh4aOYIJ0sFAXcEtui4uLiVjr5KpSBVFYDDZVrWUaKRRWSAYeK0fmKykgDXbVoNaPChRuyqdDv97czL5nXxQbq6empQmsaklkDBiNpSwFVrmr2P6UyicD5piI4f8wHh0oEm8/p4h8pyGiEWvVQd3e3nxtjAzU1NR2jP7NRBWQ8GbdEzzJAmc0V3RR4cI8Dvmwuhc8fKUFA0d6/ltHg5p+Kuaejo6OeY0jcNJ/PV00ZS0nFUoZRvvFS1bZFsKHCCQ2Pl8H0chY+C96B6ZUsrCQ1qKtwQVFRURW/QhIXMAzDPAZ6BgOr8tTa8dDxCmiYGApaJbJMxSzV+brE8pdgWkcpY5dbMF1AR9XH8/xu2ilef48bvn92n82ZwHh+8ssqTEXS9p7dHisiiURikd8PbpExNTU1UVNTA3V3Y7lC16n0gpB/NwpNcZjfa7dScC4Qh0kOQCwnlEgi3F/hMVl9fX0zvKrzSk2lfXjRhj0eT/2rvWG4+Pta3oJY7XfC3hInXAv/ldeFLx8shQ+eqQL0UAAz7ylkpej5eNZRVBWL6BU6ef14OYiY1oqyTtmsavr/5koaRucT1pzx+ZpL1+GV5nLutksUgIcmtwTRiuuVZXnU5XId7A2swJkfFsymRWC91hHg1Viw6x23+7vn9sPJ+j20BE1hCXqSWaNSQ8ScbknRZWxub1PGCw/fBV+c3AeijlUbY5bBjEqr9GuYZP4jP41WudGSC6erTRCqdGZm5i1WvXWeDHnbBCZGc2Nj4wBl/hZOwrmBBfgmlID1HmGJutHaF+tKoevp/XCgstDkjo2NtWKLuc6AVN4mNjY+s1XQxoenOoFuDPHGtnRbJj9ej5GvL0dI7+giuRyMk1giazc+DP6vgUDgOJVlOv7R+PJ12QIeL6SyeDz+Kfp8ZrNWjgDTsVjsQ7qXyTjztXJhm9ePxFLfMTg4eG9tbe1RTP9KFFYQfHliYmIS69kCC7jKYmKwxxD5P88tkVkqbPPcIps9t4T/+HjcuJ/s5BFJgf4WYABCtxGuxIZ90gAAAABJRU5ErkJggg==', 26, 26);

/**
 * Constructs a new parse dialog.
 */
var BackgroundImageDialog = function(editorUi, applyFn, img, color, showColor)
{
	var graph = editorUi.editor.graph;
	var div = document.createElement('div');
	div.style.whiteSpace = 'nowrap';

	var h3 = document.createElement('h2');
	mxUtils.write(h3, mxResources.get('background'));
	h3.style.marginTop = '0px';
	div.appendChild(h3);

	var isPageLink = img != null && img.originalSrc != null;
	var pageFound = false;

	var urlRadio = document.createElement('input');
	urlRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	urlRadio.setAttribute('value', 'url');
	urlRadio.setAttribute('type', 'radio');
	urlRadio.setAttribute('name', 'geBackgroundImageDialogOption');

	var pageRadio = document.createElement('input');
	pageRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	pageRadio.setAttribute('value', 'url');
	pageRadio.setAttribute('type', 'radio');
	pageRadio.setAttribute('name', 'geBackgroundImageDialogOption');

	var urlInput = document.createElement('input');
	urlInput.setAttribute('type', 'text');
	urlInput.style.marginBottom = '8px';
	urlInput.style.width = '360px';
	urlInput.value = (isPageLink || img == null) ? '' : img.src;
	
	var pageSelect = document.createElement('select');
	pageSelect.style.width = '360px';

	if (editorUi.pages != null)
	{
		for (var i = 0; i < editorUi.pages.length; i++)
		{
			var pageOption = document.createElement('option');
			mxUtils.write(pageOption, editorUi.pages[i].getName() ||
				mxResources.get('pageWithNumber', [i + 1]));
			pageOption.setAttribute('value', 'data:page/id,' +
				editorUi.pages[i].getId());

			if (editorUi.pages[i] == editorUi.currentPage)
			{
				pageOption.setAttribute('disabled', 'disabled');			
			}
			
			if (img != null && img.originalSrc == pageOption.getAttribute('value'))
			{
				pageOption.setAttribute('selected', 'selected');
				pageFound = true;
			}

			pageSelect.appendChild(pageOption);
		}
	}

	if (!isPageLink && (editorUi.pages == null || editorUi.pages.length == 1))
	{
		urlRadio.style.display = 'none';
		pageRadio.style.display = 'none';
		pageSelect.style.display = 'none';
	}

	var notFoundOption = document.createElement('option');
	var resetting = false;
	var ignoreEvt = false;
	
	var urlChanged = function(evt, done)
	{
		// Skips blur event if called from apply button
		if (!resetting && (evt == null || !ignoreEvt))
		{
			if (pageRadio.checked)
			{
				if (done != null)
				{
					done((notFoundOption.selected) ? null : pageSelect.value);
				}
			}
			else if (urlInput.value != '' && !editorUi.isOffline())
			{
				urlInput.value = mxUtils.trim(urlInput.value);

				editorUi.loadImage(urlInput.value, function(img)
				{
					widthInput.value = img.width;
					heightInput.value = img.height;
					
					if (done != null)
					{
						done(urlInput.value);
					}
				}, function()
				{
					editorUi.showError(mxResources.get('error'), mxResources.get('fileNotFound'), mxResources.get('ok'));
					widthInput.value = '';
					heightInput.value = '';
					
					if (done != null)
					{
						done(null);
					}
				});
			}
			else
			{
				widthInput.value = '';
				heightInput.value = '';
				
				if (done != null)
				{
					done('');
				}
			}
		}
	};

	var openFiles = mxUtils.bind(this, function(files)
	{
		editorUi.importFiles(files, 0, 0, editorUi.maxBackgroundSize, function(data, mimeType, x, y, w, h)
		{
			urlInput.value = data;
			urlChanged();
			urlInput.focus();
		}, function()
		{
			// No post processing
		}, function(file)
		{
			// Handles only images
			return file.type.substring(0, 6) == 'image/';
		}, function(queue)
		{
			// Invokes elements of queue in order
			for (var i = 0; i < queue.length; i++)
			{
				queue[i]();
			}
		}, true, editorUi.maxBackgroundBytes, editorUi.maxBackgroundBytes, true);
	});

	this.init = function()
	{
		if (isPageLink)
		{
			pageSelect.focus();
		}
		else
		{
			urlInput.focus();
		}

		mxEvent.addListener(pageSelect, 'focus', function()
		{
			urlRadio.removeAttribute('checked');
			pageRadio.setAttribute('checked', 'checked');
			pageRadio.checked = true;
		});
		
		mxEvent.addListener(urlInput, 'focus', function()
		{
			pageRadio.removeAttribute('checked');
			urlRadio.setAttribute('checked', 'checked');
			urlRadio.checked = true;
		});

		// Installs drag and drop handler for local images and links
		if (Graph.fileSupport)
		{
			urlInput.setAttribute('placeholder', mxResources.get('dragImagesHere'));
			
			// Setup the dnd listeners
			var dlg = div.parentNode;
			var dropElt = null;
				
			mxEvent.addListener(dlg, 'dragleave', function(evt)
			{
				if (dropElt != null)
			    {
			    	dropElt.parentNode.removeChild(dropElt);
			    	dropElt = null;
			    }
			    
				evt.stopPropagation();
				evt.preventDefault();
			});
			
			mxEvent.addListener(dlg, 'dragover', mxUtils.bind(this, function(evt)
			{
				if (dropElt == null)
				{
					dropElt = editorUi.highlightElement(dlg);
				}
				
				evt.stopPropagation();
				evt.preventDefault();
			}));
			
			mxEvent.addListener(dlg, 'drop', mxUtils.bind(this, function(evt)
			{
			    if (dropElt != null)
			    {
			    	dropElt.parentNode.removeChild(dropElt);
			    	dropElt = null;
			    }

			    if (evt.dataTransfer.files.length > 0)
			    {
			    	openFiles(evt.dataTransfer.files);
	    		}
			    else if (mxUtils.indexOf(evt.dataTransfer.types, 'text/uri-list') >= 0)
			    {
			    	var uri = evt.dataTransfer.getData('text/uri-list');
			    	
			    	if ((/\.(gif|jpg|jpeg|tiff|png|svg)$/i).test(uri))
					{
			    		urlInput.value = decodeURIComponent(uri);
			    		urlChanged();
					}
			    }

			    evt.stopPropagation();
			    evt.preventDefault();
			}), false);
		}
	};

	div.appendChild(urlRadio);
	div.appendChild(urlInput);
	mxUtils.br(div);

	var span = document.createElement('span');
	span.style.marginLeft = '30px';
	mxUtils.write(span, mxResources.get('width') + ':');
	div.appendChild(span);
	
	var widthInput = document.createElement('input');
	widthInput.setAttribute('type', 'text');
	widthInput.style.width = '60px';
	widthInput.style.marginLeft = '8px';
	widthInput.style.marginRight = '16px';
	widthInput.value = (img != null && !isPageLink) ? img.width : '';
	
	div.appendChild(widthInput);
	
	mxUtils.write(div, mxResources.get('height') + ':');
	
	var heightInput = document.createElement('input');
	heightInput.setAttribute('type', 'text');
	heightInput.style.width = '60px';
	heightInput.style.marginLeft = '8px';
	heightInput.style.marginRight = '16px';
	heightInput.value = (img != null && !isPageLink) ? img.height : '';
	
	div.appendChild(heightInput);
	mxUtils.br(div);
	mxUtils.br(div);

	mxEvent.addListener(urlInput, 'change', urlChanged);

	ImageDialog.filePicked = function(data)
	{
        if (data.action == google.picker.Action.PICKED)
        {
        	if (data.docs[0].thumbnails != null)
        	{
	        	var thumb = data.docs[0].thumbnails[data.docs[0].thumbnails.length - 1];
	        	
	        	if (thumb != null)
	        	{
	        		urlInput.value = thumb.url;
	        		urlChanged();
	        	}
        	}
        }
        
        urlInput.focus();
	};

	div.appendChild(pageRadio);
	div.appendChild(pageSelect);
	mxUtils.br(div);
	mxUtils.br(div);

	if (isPageLink)
	{
		pageRadio.setAttribute('checked', 'checked');
		pageRadio.checked = true;
	}
	else
	{
		urlRadio.setAttribute('checked', 'checked');
		urlRadio.checked = true;
	}

	if (!pageFound && pageRadio.checked)
	{
		mxUtils.write(notFoundOption, mxResources.get('pageNotFound'));
		notFoundOption.setAttribute('disabled', 'disabled');
		notFoundOption.setAttribute('selected', 'selected');
		notFoundOption.setAttribute('value', 'pageNotFound');
		pageSelect.appendChild(notFoundOption);
		
		mxEvent.addListener(pageSelect, 'change', function()
		{
			if (notFoundOption.parentNode != null && !notFoundOption.selected)
			{
				notFoundOption.parentNode.removeChild(notFoundOption);
			}
		});
	}

	var bgDiv = document.createElement('div');
	bgDiv.style.display = (showColor) ? 'inline-flex' : 'none';
	bgDiv.style.alignItems = 'center';
	bgDiv.style.cursor = 'default';
	bgDiv.style.minWidth = '40%';
	bgDiv.style.height = '20px';

	var cb = document.createElement('input');
	cb.setAttribute('type', 'checkbox');
	cb.style.margin = '0px 10px 0px 4px';
	cb.style.verticalAlign = 'bottom';
	cb.defaultChecked = color != mxConstants.NONE && color != null;
	cb.checked = cb.defaultChecked;
	bgDiv.appendChild(cb);

	mxUtils.write(bgDiv, mxResources.get('fillColor'));

	var shadowDiv = bgDiv.cloneNode(false);
	var shadow = document.createElement('input');
	shadow.setAttribute('type', 'checkbox');
	shadow.style.margin = '0px 10px 0px 30px';
	shadow.style.verticalAlign = 'bottom';
	shadow.defaultChecked = graph.shadowVisible;
	shadow.checked = shadow.defaultChecked;
	shadowDiv.appendChild(shadow);
	mxUtils.write(shadowDiv, mxResources.get('shadow'));

	if (!mxClient.IS_SVG || mxClient.IS_SF)
	{
		shadow.setAttribute('disabled', 'disabled');
	}

	mxEvent.addListener(shadowDiv, 'click', function(evt)
	{
		if (mxEvent.getSource(evt) != shadow)
		{
			shadow.checked = !shadow.checked;
		}
	});

	// TODO: Move createColorButton to editorUi
	var backgroundButton = document.createElement('button');
	backgroundButton.style.width = '36px';
	backgroundButton.style.height = '18px';
	backgroundButton.style.cursor = 'pointer';
	backgroundButton.style.marginLeft = '10px';
	backgroundButton.style.backgroundPosition = 'center center';
	backgroundButton.style.backgroundRepeat = 'no-repeat';
	backgroundButton.style.verticalAlign = 'bottom';
	backgroundButton.className = 'geColorBtn';
	
	var newBackgroundColor = color;
	
	function updateBackgroundColor()
	{
		if (newBackgroundColor == null || newBackgroundColor == mxConstants.NONE)
		{
			backgroundButton.style.display = 'none';
			cb.checked = false;
		}
		else
		{
			backgroundButton.style.backgroundColor = newBackgroundColor;
			backgroundButton.style.display = '';
			cb.checked = true;
		}
	};
	
	updateBackgroundColor();

	mxEvent.addListener(bgDiv, 'click', function(evt)
	{
		if (mxEvent.getSource(evt) != cb)
		{
			cb.checked = !cb.checked;
		}

		if (cb.checked)
		{
			newBackgroundColor = '#ffffff';
		}
		else
		{
			newBackgroundColor = null;
		}

		updateBackgroundColor();
	});
	
	mxEvent.addListener(backgroundButton, 'click', function(evt)
	{
		editorUi.pickColor(newBackgroundColor || 'none', function(color)
		{
			newBackgroundColor = color;
			updateBackgroundColor();
		});

		mxEvent.consume(evt);
	});
	
	bgDiv.appendChild(backgroundButton);
	div.appendChild(bgDiv);
	div.appendChild(shadowDiv);
	mxUtils.br(div);

	var btns = document.createElement('div');
	btns.style.marginTop = '30px';
	btns.style.textAlign = 'right';

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		resetting = true;
		editorUi.hideDialog();
	});
	
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}
	
	var resetBtn = mxUtils.button(mxResources.get('reset'), function()
	{
		urlInput.value = '';
		widthInput.value = '';
		heightInput.value = '';
		urlRadio.checked = true;
		newBackgroundColor = mxConstants.NONE;
		updateBackgroundColor();
		resetting = false;
	});
	mxEvent.addGestureListeners(resetBtn, function()
	{
		// Blocks processing a image URL while clicking reset
		resetting = true;
	});
	resetBtn.className = 'geBtn';
	resetBtn.width = '100';
	btns.appendChild(resetBtn);

	if (Graph.fileSupport)
	{
		var fileInput = document.createElement('input');
		fileInput.setAttribute('multiple', 'multiple');
		fileInput.setAttribute('type', 'file');
		
		mxEvent.addListener(fileInput, 'change', function(evt)
		{
			if (fileInput.files != null)
			{
				openFiles(fileInput.files);
				
				// Resets input to force change event for same file (type reset required for IE)
				fileInput.type = '';
				fileInput.type = 'file';
				fileInput.value = '';
			}
		});
		
		fileInput.style.display = 'none';
		div.appendChild(fileInput);
		
		var btn = mxUtils.button(mxResources.get('open'), function()
		{
			fileInput.click();
		});

		btn.className = 'geBtn';
		btns.appendChild(btn);
	}

	applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		editorUi.hideDialog();
		
		urlChanged(null, function(url)
		{
			applyFn((url != '' && url != null) ? new mxImage(url, widthInput.value,
				heightInput.value) : null, url == null, newBackgroundColor,
				(!mxClient.IS_SVG || mxClient.IS_SF) ? null : shadow.checked);
		});
	});
	
	mxEvent.addGestureListeners(applyBtn, function()
	{
		ignoreEvt = true;
	});
	
	applyBtn.className = 'geBtn gePrimaryBtn';
	btns.appendChild(applyBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	div.appendChild(btns);

	this.container = div;
};

/**
 * Constructs a new parse dialog.
 */
var ParseDialog = function(editorUi, title, defaultType)
{
	var plantUmlExample = '@startuml\nskinparam shadowing false\nAlice -> Bob: Authentication Request\nBob --> Alice: Authentication Response\n\n' +
		'Alice -> Bob: Another authentication Request\nAlice <-- Bob: Another authentication Response\n@enduml';
	var insertPoint = editorUi.editor.graph.getFreeInsertPoint();

	function parse(text, type, evt)
	{
		if (type == 'plantUmlPng' || type == 'plantUmlSvg' || type == 'plantUmlTxt')
		{
			if (editorUi.spinner.spin(document.body, mxResources.get('inserting')))
			{
				var graph = editorUi.editor.graph;
				var format = (type == 'plantUmlTxt') ? 'txt' :
					((type == 'plantUmlPng') ? 'png' : 'svg');
				
				function insertPlantUmlImage(text, format, data, w, h)
				{
					var cell = null;
					
					graph.getModel().beginUpdate();
					try
					{
						cell = (format == 'txt') ?
							editorUi.insertAsPreText(data, insertPoint.x, insertPoint.y) :
							graph.insertVertex(null, null, null, insertPoint.x, insertPoint.y,
								w, h, 'shape=image;noLabel=1;verticalAlign=top;aspect=fixed;imageAspect=0;' +
								'image=' + editorUi.convertDataUri(data) + ';')
						graph.setAttributeForCell(cell, 'plantUmlData',
							JSON.stringify({data: text, format: format},
							null, 2));
					}
					finally
					{
						graph.getModel().endUpdate();
					}
					
					if (cell != null)
					{
						graph.setSelectionCell(cell);
						graph.scrollCellToVisible(cell);
					}
				};
				
				// Hardcoded response for default settings
				if (text == plantUmlExample && format == 'svg')
				{
					window.setTimeout(function()
					{
						editorUi.spinner.stop();
						insertPlantUmlImage(text, format, 'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiBjb250ZW50U2NyaXB0VHlwZT0iYXBwbGljYXRpb24vZWNtYXNjcmlwdCIgY29udGVudFN0eWxlVHlwZT0idGV4dC9jc3MiIGhlaWdodD0iMjEycHgiIHByZXNlcnZlQXNwZWN0UmF0aW89Im5vbmUiIHN0eWxlPSJ3aWR0aDoyOTVweDtoZWlnaHQ6MjEycHg7IiB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCAyOTUgMjEyIiB3aWR0aD0iMjk1cHgiIHpvb21BbmRQYW49Im1hZ25pZnkiPjxkZWZzLz48Zz48bGluZSBzdHlsZT0ic3Ryb2tlOiAjQTgwMDM2OyBzdHJva2Utd2lkdGg6IDEuMDsgc3Ryb2tlLWRhc2hhcnJheTogNS4wLDUuMDsiIHgxPSIzMSIgeDI9IjMxIiB5MT0iMzQuNDg4MyIgeTI9IjE3MS43MzA1Ii8+PGxpbmUgc3R5bGU9InN0cm9rZTogI0E4MDAzNjsgc3Ryb2tlLXdpZHRoOiAxLjA7IHN0cm9rZS1kYXNoYXJyYXk6IDUuMCw1LjA7IiB4MT0iMjY0LjUiIHgyPSIyNjQuNSIgeTE9IjM0LjQ4ODMiIHkyPSIxNzEuNzMwNSIvPjxyZWN0IGZpbGw9IiNGRUZFQ0UiIGhlaWdodD0iMzAuNDg4MyIgc3R5bGU9InN0cm9rZTogI0E4MDAzNjsgc3Ryb2tlLXdpZHRoOiAxLjU7IiB3aWR0aD0iNDciIHg9IjgiIHk9IjMiLz48dGV4dCBmaWxsPSIjMDAwMDAwIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgbGVuZ3RoQWRqdXN0PSJzcGFjaW5nQW5kR2x5cGhzIiB0ZXh0TGVuZ3RoPSIzMyIgeD0iMTUiIHk9IjIzLjUzNTIiPkFsaWNlPC90ZXh0PjxyZWN0IGZpbGw9IiNGRUZFQ0UiIGhlaWdodD0iMzAuNDg4MyIgc3R5bGU9InN0cm9rZTogI0E4MDAzNjsgc3Ryb2tlLXdpZHRoOiAxLjU7IiB3aWR0aD0iNDciIHg9IjgiIHk9IjE3MC43MzA1Ii8+PHRleHQgZmlsbD0iIzAwMDAwMCIgZm9udC1mYW1pbHk9InNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMTQiIGxlbmd0aEFkanVzdD0ic3BhY2luZ0FuZEdseXBocyIgdGV4dExlbmd0aD0iMzMiIHg9IjE1IiB5PSIxOTEuMjY1NiI+QWxpY2U8L3RleHQ+PHJlY3QgZmlsbD0iI0ZFRkVDRSIgaGVpZ2h0PSIzMC40ODgzIiBzdHlsZT0ic3Ryb2tlOiAjQTgwMDM2OyBzdHJva2Utd2lkdGg6IDEuNTsiIHdpZHRoPSI0MCIgeD0iMjQ0LjUiIHk9IjMiLz48dGV4dCBmaWxsPSIjMDAwMDAwIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgbGVuZ3RoQWRqdXN0PSJzcGFjaW5nQW5kR2x5cGhzIiB0ZXh0TGVuZ3RoPSIyNiIgeD0iMjUxLjUiIHk9IjIzLjUzNTIiPkJvYjwvdGV4dD48cmVjdCBmaWxsPSIjRkVGRUNFIiBoZWlnaHQ9IjMwLjQ4ODMiIHN0eWxlPSJzdHJva2U6ICNBODAwMzY7IHN0cm9rZS13aWR0aDogMS41OyIgd2lkdGg9IjQwIiB4PSIyNDQuNSIgeT0iMTcwLjczMDUiLz48dGV4dCBmaWxsPSIjMDAwMDAwIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgbGVuZ3RoQWRqdXN0PSJzcGFjaW5nQW5kR2x5cGhzIiB0ZXh0TGVuZ3RoPSIyNiIgeD0iMjUxLjUiIHk9IjE5MS4yNjU2Ij5Cb2I8L3RleHQ+PHBvbHlnb24gZmlsbD0iI0E4MDAzNiIgcG9pbnRzPSIyNTIuNSw2MS43OTg4LDI2Mi41LDY1Ljc5ODgsMjUyLjUsNjkuNzk4OCwyNTYuNSw2NS43OTg4IiBzdHlsZT0ic3Ryb2tlOiAjQTgwMDM2OyBzdHJva2Utd2lkdGg6IDEuMDsiLz48bGluZSBzdHlsZT0ic3Ryb2tlOiAjQTgwMDM2OyBzdHJva2Utd2lkdGg6IDEuMDsiIHgxPSIzMS41IiB4Mj0iMjU4LjUiIHkxPSI2NS43OTg4IiB5Mj0iNjUuNzk4OCIvPjx0ZXh0IGZpbGw9IiMwMDAwMDAiIGZvbnQtZmFtaWx5PSJzYW5zLXNlcmlmIiBmb250LXNpemU9IjEzIiBsZW5ndGhBZGp1c3Q9InNwYWNpbmdBbmRHbHlwaHMiIHRleHRMZW5ndGg9IjE0NyIgeD0iMzguNSIgeT0iNjEuMDU2NiI+QXV0aGVudGljYXRpb24gUmVxdWVzdDwvdGV4dD48cG9seWdvbiBmaWxsPSIjQTgwMDM2IiBwb2ludHM9IjQyLjUsOTEuMTA5NCwzMi41LDk1LjEwOTQsNDIuNSw5OS4xMDk0LDM4LjUsOTUuMTA5NCIgc3R5bGU9InN0cm9rZTogI0E4MDAzNjsgc3Ryb2tlLXdpZHRoOiAxLjA7Ii8+PGxpbmUgc3R5bGU9InN0cm9rZTogI0E4MDAzNjsgc3Ryb2tlLXdpZHRoOiAxLjA7IHN0cm9rZS1kYXNoYXJyYXk6IDIuMCwyLjA7IiB4MT0iMzYuNSIgeDI9IjI2My41IiB5MT0iOTUuMTA5NCIgeTI9Ijk1LjEwOTQiLz48dGV4dCBmaWxsPSIjMDAwMDAwIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxMyIgbGVuZ3RoQWRqdXN0PSJzcGFjaW5nQW5kR2x5cGhzIiB0ZXh0TGVuZ3RoPSIxNTciIHg9IjQ4LjUiIHk9IjkwLjM2NzIiPkF1dGhlbnRpY2F0aW9uIFJlc3BvbnNlPC90ZXh0Pjxwb2x5Z29uIGZpbGw9IiNBODAwMzYiIHBvaW50cz0iMjUyLjUsMTIwLjQxOTksMjYyLjUsMTI0LjQxOTksMjUyLjUsMTI4LjQxOTksMjU2LjUsMTI0LjQxOTkiIHN0eWxlPSJzdHJva2U6ICNBODAwMzY7IHN0cm9rZS13aWR0aDogMS4wOyIvPjxsaW5lIHN0eWxlPSJzdHJva2U6ICNBODAwMzY7IHN0cm9rZS13aWR0aDogMS4wOyIgeDE9IjMxLjUiIHgyPSIyNTguNSIgeTE9IjEyNC40MTk5IiB5Mj0iMTI0LjQxOTkiLz48dGV4dCBmaWxsPSIjMDAwMDAwIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIxMyIgbGVuZ3RoQWRqdXN0PSJzcGFjaW5nQW5kR2x5cGhzIiB0ZXh0TGVuZ3RoPSIxOTkiIHg9IjM4LjUiIHk9IjExOS42Nzc3Ij5Bbm90aGVyIGF1dGhlbnRpY2F0aW9uIFJlcXVlc3Q8L3RleHQ+PHBvbHlnb24gZmlsbD0iI0E4MDAzNiIgcG9pbnRzPSI0Mi41LDE0OS43MzA1LDMyLjUsMTUzLjczMDUsNDIuNSwxNTcuNzMwNSwzOC41LDE1My43MzA1IiBzdHlsZT0ic3Ryb2tlOiAjQTgwMDM2OyBzdHJva2Utd2lkdGg6IDEuMDsiLz48bGluZSBzdHlsZT0ic3Ryb2tlOiAjQTgwMDM2OyBzdHJva2Utd2lkdGg6IDEuMDsgc3Ryb2tlLWRhc2hhcnJheTogMi4wLDIuMDsiIHgxPSIzNi41IiB4Mj0iMjYzLjUiIHkxPSIxNTMuNzMwNSIgeTI9IjE1My43MzA1Ii8+PHRleHQgZmlsbD0iIzAwMDAwMCIgZm9udC1mYW1pbHk9InNhbnMtc2VyaWYiIGZvbnQtc2l6ZT0iMTMiIGxlbmd0aEFkanVzdD0ic3BhY2luZ0FuZEdseXBocyIgdGV4dExlbmd0aD0iMjA5IiB4PSI0OC41IiB5PSIxNDguOTg4MyI+QW5vdGhlciBhdXRoZW50aWNhdGlvbiBSZXNwb25zZTwvdGV4dD48IS0tTUQ1PVs3ZjNlNGQwYzkwMWVmZGJjNTdlYjQ0MjQ5YTNiODE5N10KQHN0YXJ0dW1sDQpza2lucGFyYW0gc2hhZG93aW5nIGZhbHNlDQpBbGljZSAtPiBCb2I6IEF1dGhlbnRpY2F0aW9uIFJlcXVlc3QNCkJvYiAtIC0+IEFsaWNlOiBBdXRoZW50aWNhdGlvbiBSZXNwb25zZQ0KDQpBbGljZSAtPiBCb2I6IEFub3RoZXIgYXV0aGVudGljYXRpb24gUmVxdWVzdA0KQWxpY2UgPC0gLSBCb2I6IEFub3RoZXIgYXV0aGVudGljYXRpb24gUmVzcG9uc2UNCkBlbmR1bWwNCgpQbGFudFVNTCB2ZXJzaW9uIDEuMjAyMC4wMihTdW4gTWFyIDAxIDA0OjIyOjA3IENTVCAyMDIwKQooTUlUIHNvdXJjZSBkaXN0cmlidXRpb24pCkphdmEgUnVudGltZTogT3BlbkpESyBSdW50aW1lIEVudmlyb25tZW50CkpWTTogT3BlbkpESyA2NC1CaXQgU2VydmVyIFZNCkphdmEgVmVyc2lvbjogMTIrMzMKT3BlcmF0aW5nIFN5c3RlbTogTWFjIE9TIFgKRGVmYXVsdCBFbmNvZGluZzogVVRGLTgKTGFuZ3VhZ2U6IGVuCkNvdW50cnk6IFVTCi0tPjwvZz48L3N2Zz4=',
							295, 212);
					}, 200);
					
				}
				else
				{
					editorUi.generatePlantUmlImage(text, format, function(data, w, h)
					{
						editorUi.spinner.stop();
						insertPlantUmlImage(text, format, data, w, h);
						
					}, function(e)
					{
						editorUi.handleError(e);
					});
				}
			}
		}
		else if (type == 'mermaid' || type == 'mermaid2drawio')
		{
			if (editorUi.spinner.spin(document.body, mxResources.get('inserting')))
			{
				editorUi.parseMermaidDiagram(text, null, mxUtils.bind(this, function(xml)
				{
					editorUi.spinner.stop();
					var graph = editorUi.editor.graph;
					graph.setSelectionCells(
						editorUi.importXml(xml,
						Math.max(insertPoint.x, 20),
						Math.max(insertPoint.y, 20),
						true, null, null, true));
					graph.scrollCellToVisible(graph.getSelectionCell());
				}), mxUtils.bind(this, function(e)
				{
					editorUi.handleError(e);
				}), null, type == 'mermaid2drawio');
			}
		}
		else if (type == 'table')
		{
			var lines = text.split('\n');
			var tableCell = null;
			var cells = [];
			var dx = 0;
			var pkMap = {};

			//First pass to find primary keys
			for (var i = 0; i < lines.length; i++)
			{
				var line = mxUtils.trim(lines[i]);
				
				if (line.substring(0, 11).toLowerCase() == 'primary key')
				{
					var pk = line.match(/\((.+)\)/);
					
					if (pk && pk[1])
					{
						pkMap[pk[1]] = true;						
					}
					
					lines.splice(i, 1);
				}
				else if (line.toLowerCase().indexOf('primary key') > 0)
				{
					pkMap[line.split(' ')[0]] = true;
					lines[i] = mxUtils.trim(line.replace(/primary key/i, ''));
				}
			}
			
			for (var i = 0; i < lines.length; i++)
			{
				var tmp = mxUtils.trim(lines[i]);
				
				if (tmp.substring(0, 12).toLowerCase() == 'create table')
				{
					var name = mxUtils.trim(tmp.substring(12));
					
					if (name.charAt(name.length - 1) == '(')
					{
						name = mxUtils.trim(name.substring(0, name.length - 1));
					}
					
					tableCell = new mxCell(name, new mxGeometry(dx, 0, 160, 30),
						'shape=table;startSize=30;container=1;collapsible=1;childLayout=tableLayout;' +
						'fixedRows=1;rowLines=0;fontStyle=1;align=center;resizeLast=1;');
					tableCell.vertex = true;
					cells.push(tableCell);
					
					var size = editorUi.editor.graph.getPreferredSizeForCell(rowCell);
		   			
		   			if (size != null)
		   			{
		   				tableCell.geometry.width = size.width + 10;
		   			}
				}
				else if (tableCell != null && tmp.charAt(0) == ')')
				{
					dx += tableCell.geometry.width + 40;
					tableCell = null;
				}
				else if (tmp != '(' && tableCell != null)
				{
					var name = tmp.substring(0, (tmp.charAt(tmp.length - 1) == ',') ? tmp.length - 1 : tmp.length);
				
					var pk = pkMap[name.split(' ')[0]];
					var rowCell = new mxCell('', new mxGeometry(0, 0, 160, 30),
						'shape=tableRow;horizontal=0;startSize=0;swimlaneHead=0;swimlaneBody=0;fillColor=none;' +
						'collapsible=0;dropTarget=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;' +
						'strokeColor=inherit;top=0;left=0;right=0;bottom=' + (pk ? '1' : '0') + ';');
					rowCell.vertex = true;
					
					var left = new mxCell(pk ? 'PK' : '', new mxGeometry(0, 0, 30, 30),
						'shape=partialRectangle;overflow=hidden;connectable=0;fillColor=none;' +
						'strokeColor=inherit;top=0;left=0;bottom=0;right=0;' +
						(pk ? 'fontStyle=1;' : ''));
					left.vertex = true;
					rowCell.insert(left);
					
					var right = new mxCell(name, new mxGeometry(30, 0, 130, 30),
						'shape=partialRectangle;overflow=hidden;connectable=0;fillColor=none;align=left;' +
						'strokeColor=inherit;top=0;left=0;bottom=0;right=0;spacingLeft=6;' +
						(pk ? 'fontStyle=5;' : ''));
					right.vertex = true;
					rowCell.insert(right);
					
		   			var size = editorUi.editor.graph.getPreferredSizeForCell(right);
		   			
		   			if (size != null && tableCell.geometry.width < size.width + 30)
		   			{
		   				tableCell.geometry.width = Math.min(320, Math.max(tableCell.geometry.width, size.width + 30));
		   			}
		   			
		   			tableCell.insert(rowCell, pk? 0 : null);
		   			tableCell.geometry.height += 30;
				}
			}
			
			if (cells.length > 0)
			{
				var graph = editorUi.editor.graph;
				graph.setSelectionCells(graph.importCells(cells, insertPoint.x, insertPoint.y));
				graph.scrollCellToVisible(graph.getSelectionCell());
			}
		}
		else if (type == 'list')
		{
			var lines = text.split('\n');
			
			if (lines.length > 0)
			{
				var graph = editorUi.editor.graph;
				var listCell = null;
				var cells = [];
				var x0 = 0;

				for (var i = 0; i < lines.length; i++)
				{
					if (lines[i].charAt(0) != ';')
					{
						if (lines[i].length == 0)
						{
							listCell = null;
						}
						else
						{
							if (listCell == null)
							{
								listCell = new mxCell(lines[i], new mxGeometry(x0, 0, 160, 26 + 4),
									'swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;startSize=26;horizontalStack=0;resizeParent=1;resizeParentMax=0;resizeLast=0;collapsible=1;marginBottom=0;');
								listCell.vertex = true;
								cells.push(listCell);

								var size = graph.getPreferredSizeForCell(listCell);
						
					   			if (size != null && listCell.geometry.width < size.width + 10)
					   			{
					   				listCell.geometry.width = size.width + 10;
					   			}
					   			
					   			x0 += listCell.geometry.width + 40;
							}
							else if (lines[i] == '--')
							{
								var divider = new mxCell('', new mxGeometry(0, 0, 40, 8), 'line;strokeWidth=1;fillColor=none;align=left;verticalAlign=middle;spacingTop=-1;spacingLeft=3;spacingRight=3;rotatable=0;labelPosition=right;points=[];portConstraint=eastwest;');
								divider.vertex = true;
								listCell.geometry.height += divider.geometry.height;
								listCell.insert(divider);
							}
							else if (lines[i].length > 0)
							{
								var field = new mxCell(lines[i], new mxGeometry(0, 0, 60, 26), 'text;strokeColor=none;fillColor=none;align=left;verticalAlign=top;spacingLeft=4;spacingRight=4;overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;');
								field.vertex = true;
								
								var size = graph.getPreferredSizeForCell(field);
			   			
					   			if (size != null && field.geometry.width < size.width)
					   			{
					   				field.geometry.width = size.width;
					   			}
								
					   			listCell.geometry.width = Math.max(listCell.geometry.width, field.geometry.width);
								listCell.geometry.height += field.geometry.height;
								listCell.insert(field);
							}
						}
					}
				}
				
				if (cells.length > 0)
				{
					graph.getModel().beginUpdate();
					try
					{
						cells = graph.importCells(cells, insertPoint.x, insertPoint.y);
						var inserted = [];
						
						for (var i = 0; i < cells.length; i++)
						{
							inserted.push(cells[i]);
							inserted = inserted.concat(cells[i].children);
						}
						
						graph.fireEvent(new mxEventObject('cellsInserted', 'cells', inserted));
					}
					finally
					{
						graph.getModel().endUpdate();
					}
					
					graph.setSelectionCells(cells);
					graph.scrollCellToVisible(graph.getSelectionCell());
				}
			}
		}
		else
		{
			var lines = text.split('\n');
			var vertices = new Object();
			var cells = [];
			
			function getOrCreateVertex(id)
			{
				var vertex = vertices[id];
	
				if (vertex == null)
				{
					vertex = new mxCell(id, new mxGeometry(0, 0, 80, 30), 'whiteSpace=wrap;html=1;');
					vertex.vertex = true;
					vertices[id] = vertex;
					cells.push(vertex);
				}
				
				return vertex;
			};
			
			for (var i = 0; i < lines.length; i++)
			{
				if (lines[i].charAt(0) != ';')
				{
					var values = lines[i].split('->');
					
					if (values.length >= 2)
					{
						var source = getOrCreateVertex(values[0]);
						var target = getOrCreateVertex(values[values.length - 1]);
						
						var edge = new mxCell((values.length > 2) ? values[1] : '', new mxGeometry());
						edge.edge = true;
						edge.geometry.relative = true;
						source.insertEdge(edge, true);
						target.insertEdge(edge, false);
						cells.push(edge);
					}
				}
			}
			
			if (cells.length > 0)
			{
				var container = document.createElement('div');
				container.style.visibility = 'hidden';
				document.body.appendChild(container);
				
				// Temporary graph for running the layout
				var graph = new Graph(container);
				
				graph.getModel().beginUpdate();
				try
				{
					cells = graph.importCells(cells);
					
					for (var i = 0; i < cells.length; i++)
					{
						if (graph.getModel().isVertex(cells[i]))
						{
							var size = graph.getPreferredSizeForCell(cells[i]);
							cells[i].geometry.width = Math.max(cells[i].geometry.width, size.width);
							cells[i].geometry.height = Math.max(cells[i].geometry.height, size.height);
						}
					}

					var runEdgeLayout = true;

					if (type == 'horizontalFlow' || type == 'verticalFlow')
					{
						var flowLayout = new mxHierarchicalLayout(graph,
							(type == 'horizontalFlow') ?
							mxConstants.DIRECTION_WEST :
							mxConstants.DIRECTION_NORTH);
						flowLayout.execute(graph.getDefaultParent(), cells);
						runEdgeLayout = false;
					}
					else if (type == 'circle')
					{
						var circleLayout = new mxCircleLayout(graph);
						circleLayout.execute(graph.getDefaultParent());
					}
					else
					{
						var layout = new mxFastOrganicLayout(graph);
						layout.disableEdgeStyle = false;
						layout.forceConstant = 180;
						layout.execute(graph.getDefaultParent());
					}
					
					if (runEdgeLayout)
					{
						var edgeLayout = new mxParallelEdgeLayout(graph);
						edgeLayout.spacing = 30;
						edgeLayout.execute(graph.getDefaultParent());
					}
				}
				finally
				{
					graph.getModel().endUpdate();
				}
				
				graph.clearCellOverlays();
				
				// Copy to actual graph
				var inserted = [];
				
				editorUi.editor.graph.getModel().beginUpdate();
				try
				{
					cells = graph.getModel().getChildren(graph.getDefaultParent());
					inserted = editorUi.editor.graph.importCells(cells, insertPoint.x, insertPoint.y)
					editorUi.editor.graph.fireEvent(new mxEventObject('cellsInserted', 'cells', inserted));
				}
				finally
				{
					editorUi.editor.graph.getModel().endUpdate();
				}

				editorUi.editor.graph.setSelectionCells(inserted);
				editorUi.editor.graph.scrollCellToVisible(editorUi.editor.graph.getSelectionCell());
				graph.destroy();
				container.parentNode.removeChild(container);
			}
		}
	};
	
	var div = document.createElement('div');
	
	var textarea = document.createElement('textarea');
	textarea.style.position = 'absolute';
	textarea.style.left = '30px';
	textarea.style.right = '30px';
	textarea.style.top = '30px';
	textarea.style.bottom = '100px';
	textarea.style.resize = 'none';

	textarea.setAttribute('wrap', 'off');
	textarea.setAttribute('spellcheck', 'false');
	textarea.setAttribute('autocorrect', 'off');
	textarea.setAttribute('autocomplete', 'off');
	textarea.setAttribute('autocapitalize', 'off');
	
	var typeSelect = document.createElement('select');
	typeSelect.className = 'geBtn';
	
	if (defaultType == 'formatSql' || (defaultType == 'mermaid' && 
		editorUi.getServiceName() != 'draw.io' &&
		editorUi.getServiceName() != 'atlassian'))
	{
		typeSelect.style.display = 'none';
	}

	var listOption = document.createElement('option');
	listOption.setAttribute('value', 'list');
	mxUtils.write(listOption, mxResources.get('list'));
	
	if (defaultType != 'plantUml' && defaultType != 'mermaid')
	{
		typeSelect.appendChild(listOption);
	}

	if (defaultType == null || defaultType == 'fromText')
	{
		listOption.setAttribute('selected', 'selected');
	}
	
	var tableOption = document.createElement('option');
	tableOption.setAttribute('value', 'table');
	mxUtils.write(tableOption, mxResources.get('formatSql'));
	
	if (defaultType == 'formatSql')
	{
		typeSelect.appendChild(tableOption);
		tableOption.setAttribute('selected', 'selected');
	}

	var mermaidOption = document.createElement('option');
	mermaidOption.setAttribute('value', 'mermaid');
	mxUtils.write(mermaidOption, mxResources.get('image'));

	if (defaultType == 'mermaid')
	{
		if (typeof mxMermaidToDrawio !== 'undefined')
		{
			var mermaid2drawioOption = document.createElement('option');
			mermaid2drawioOption.setAttribute('value', 'mermaid2drawio');
			mermaid2drawioOption.setAttribute('selected', 'selected');
			mxUtils.write(mermaid2drawioOption, mxResources.get('diagram'));
			typeSelect.appendChild(mermaid2drawioOption);
		}
		else
		{
			typeSelect.style.display = 'none';
		}
		
		typeSelect.appendChild(mermaidOption);
	}

	var diagramOption = document.createElement('option');
	diagramOption.setAttribute('value', 'diagram');
	mxUtils.write(diagramOption, mxResources.get('diagram'));

	var circleOption = document.createElement('option');
	circleOption.setAttribute('value', 'circle');
	mxUtils.write(circleOption, mxResources.get('circle'));

	var horizontalFlowOption = document.createElement('option');
	horizontalFlowOption.setAttribute('value', 'horizontalFlow');
	mxUtils.write(horizontalFlowOption, mxResources.get('horizontalFlow'));
	
	var verticalFlowOption = document.createElement('option');
	verticalFlowOption.setAttribute('value', 'verticalFlow');
	mxUtils.write(verticalFlowOption, mxResources.get('verticalFlow'));
	
	if (defaultType != 'plantUml' && defaultType != 'mermaid')
	{
		typeSelect.appendChild(diagramOption);
		typeSelect.appendChild(circleOption);
		typeSelect.appendChild(horizontalFlowOption);
		typeSelect.appendChild(verticalFlowOption);
	}

	var plantUmlSvgOption = document.createElement('option');
	plantUmlSvgOption.setAttribute('value', 'plantUmlSvg');
	mxUtils.write(plantUmlSvgOption, mxResources.get('plantUml') +
		' (' + mxResources.get('formatSvg') + ')');
	
	if (defaultType == 'plantUml')
	{
		plantUmlSvgOption.setAttribute('selected', 'selected');
	}
	
	var plantUmlPngOption = document.createElement('option');
	plantUmlPngOption.setAttribute('value', 'plantUmlPng');
	mxUtils.write(plantUmlPngOption, mxResources.get('plantUml') +
		' (' + mxResources.get('formatPng') + ')');
	
	var plantUmlTxtOption = document.createElement('option');
	plantUmlTxtOption.setAttribute('value', 'plantUmlTxt');
	mxUtils.write(plantUmlTxtOption, mxResources.get('plantUml') +
		' (' + mxResources.get('text') + ')');
	
	// Disabled for invalid hosts via CORS headers
	if (EditorUi.enablePlantUml && Graph.fileSupport &&
		!editorUi.isOffline() && defaultType == 'plantUml')
	{
		typeSelect.appendChild(plantUmlSvgOption);
		typeSelect.appendChild(plantUmlPngOption);
		typeSelect.appendChild(plantUmlTxtOption);
	}

	function getDefaultValue()
	{
		if (typeSelect.value == 'list')
		{
			return 'Person\n-name: String\n-birthDate: Date\n--\n+getName(): String\n+setName(String): void\n+isBirthday(): boolean\n\n' +
				'Address\n-street: String\n-city: String\n-state: String';
		}
		else if (typeSelect.value == 'mermaid' || typeSelect.value == 'mermaid2drawio')
		{
			return 'graph TD;\n  A-->B;\n  A-->C;\n  B-->D;\n  C-->D;';
		}
		else if (typeSelect.value == 'table')
		{
			return 'CREATE TABLE Suppliers\n(\nsupplier_id int NOT NULL PRIMARY KEY,\n' +
				'supplier_name char(50) NOT NULL,\ncontact_name char(50),\n);\n' +
				'CREATE TABLE Customers\n(\ncustomer_id int NOT NULL PRIMARY KEY,\n' +
				'customer_name char(50) NOT NULL,\naddress char(50),\n' +
				'city char(50),\nstate char(25),\nzip_code char(10)\n);\n';
		}
		else if (typeSelect.value == 'plantUmlPng')
		{
			return '@startuml\nskinparam backgroundcolor transparent\nskinparam shadowing false\nAlice -> Bob: Authentication Request\nBob --> Alice: Authentication Response\n\nAlice -> Bob: Another authentication Request\nAlice <-- Bob: Another authentication Response\n@enduml';
		}
		else if (typeSelect.value == 'plantUmlSvg' || typeSelect.value == 'plantUmlTxt')
		{
			return plantUmlExample;
		}
		else
		{
			return ';Example:\na->b\nb->edge label->c\nc->a\n';
		}
	};
	
	var defaultValue = getDefaultValue();
	textarea.value = defaultValue;
	div.appendChild(textarea);

	var buttons = document.createElement('div');
	buttons.style.position = 'absolute';
	buttons.style.bottom = '46px';
	buttons.style.right = '30px';
	buttons.style.left = '30px';
	buttons.style.justifyContent = 'end';
	buttons.style.display = 'flex';
	
	this.init = function()
	{
		textarea.focus();
	};
	
	// Enables dropping files
	if (Graph.fileSupport)
	{
		function handleDrop(evt)
		{
		    evt.stopPropagation();
		    evt.preventDefault();
		    
		    if (evt.dataTransfer.files.length > 0)
		    {
		    	var file = evt.dataTransfer.files[0];
    			
				var reader = new FileReader();
				reader.onload = function(e) { textarea.value = e.target.result; };
				reader.readAsText(file);
    		}
		};
		
		function handleDragOver(evt)
		{
			evt.stopPropagation();
			evt.preventDefault();
		};

		// Setup the dnd listeners.
		textarea.addEventListener('dragover', handleDragOver, false);
		textarea.addEventListener('drop', handleDrop, false);
	}

	if (defaultType == 'plantUml' && window.PLANT_URL == 'https://plant-aws.diagrams.net')
	{
		let warning = document.createElement('div');
		warning.style.display = 'inline-block';
		warning.style.color = '#c00';
		warning.style.fontSize = '12px';
		warning.style.marginRight = '10px';
		warning.style.display = 'flex';
		warning.style.alignItems = 'center';
		warning.innerHTML = '<a href="https://github.com/jgraph/plantuml-converter/tree/main/plantuml-to-drawio" target="_blank" ' +
			'rel="noopener noreferrer">PlantUML project changes in 2026</a>';
		buttons.appendChild(warning);
	}

	if ((!editorUi.isOffline() || mxClient.IS_CHROMEAPP) &&
		(defaultType == 'mermaid' || defaultType == 'plantUml'))
	{
		buttons.appendChild(editorUi.createHelpIcon(
			(defaultType == 'mermaid') ?
				'https://mermaid.js.org/intro/' :
				'https://plantuml.com/'));
	}
	
	buttons.appendChild(typeSelect);
	
	mxEvent.addListener(typeSelect, 'change', function()
	{
		var newDefaultValue = getDefaultValue();
		
		if (textarea.value.length == 0 || textarea.value == defaultValue)
		{
			defaultValue = newDefaultValue;
			textarea.value = defaultValue;
		}
	});
	
	var cancelBtn = mxUtils.button(mxResources.get('close'), function()
	{
		if (textarea.value == defaultValue)
		{
			editorUi.hideDialog();
		}
		else
		{
			editorUi.confirm(mxResources.get('areYouSure'), function()
			{
				editorUi.hideDialog();
			});
		}
	});
	
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}
	
	var okBtn = mxUtils.button(mxResources.get('insert'), function(evt)
	{
		try
		{
			editorUi.hideDialog();
			parse(textarea.value, typeSelect.value, evt);
		}
		catch (e)
		{
			editorUi.handleError(e);
		}
	});
	
	okBtn.className = 'geBtn gePrimaryBtn';
	buttons.appendChild(okBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}

	div.appendChild(buttons);
	this.container = div;
};

/**
 * Constructs a new dialog for creating files from templates.
 */
var NewDialog = function(editorUi, compact, showName, callback, createOnly, cancelCallback,
		leftHighlight, rightHighlight, rightHighlightBorder, itemPadding, templateFile,
		recentDocsCallback, searchDocsCallback, openExtDocCallback, showImport, createButtonLabel,
		customTempCallback, withoutType, generatePrompt, noBlank)
{
	var ww = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
	var smallScreen = ww < 500;
	showName = (showName != null) ? showName : true;
	createOnly = (createOnly != null) ? createOnly : false;
	leftHighlight = (leftHighlight != null) ? leftHighlight : 'light-dark(#ebf2f9, ' + Editor.darkColor + ')';
	rightHighlight = (rightHighlight != null) ? rightHighlight : 'light-dark(#e6eff8, #ffffff)';
	rightHighlightBorder = (rightHighlightBorder != null) ? rightHighlightBorder :
		'2px dashed light-dark(#29b6f2, #00a8ff)';
	templateFile = (templateFile != null) ? templateFile : EditorUi.templateFile;

	// Handles click on insert while entering generate prompt
	var insertWasPressed = false;
	
	var outer = document.createElement('div');
	outer.style.userSelect = 'none';
	outer.style.height = '100%';
	
	var header = document.createElement('div');
	header.style.whiteSpace = 'nowrap';
	header.style.height = '46px';
	
	if (showName)
	{
		outer.appendChild(header);
	}
	
	var logo = document.createElement('img');
	logo.setAttribute('border', '0');
	logo.setAttribute('align', 'absmiddle');
	logo.style.width = '40px';
	logo.style.height = '40px';
	logo.style.marginRight = '10px';
	logo.style.paddingBottom = '4px';
	
	if (editorUi.mode == App.MODE_GOOGLE)
	{
		logo.src = IMAGE_PATH + '/google-drive-logo.svg';
	}
	else if (editorUi.mode == App.MODE_DROPBOX)
	{
		logo.src = IMAGE_PATH + '/dropbox-logo.svg';
	}
	else if (editorUi.mode == App.MODE_ONEDRIVE)
	{
		logo.src = IMAGE_PATH + '/onedrive-logo.svg';
	}
	else if (editorUi.mode == App.MODE_M365)
	{
		logo.src = IMAGE_PATH + '/onedrive-logo.svg';
	}
	else if (editorUi.mode == App.MODE_GITHUB)
	{
		logo.src = IMAGE_PATH + '/github-logo.svg';
	}
	else if (editorUi.mode == App.MODE_GITLAB)
	{
		logo.src = IMAGE_PATH + '/gitlab-logo.svg';
	}
	else if (editorUi.mode == App.MODE_TRELLO)
	{
		logo.src = IMAGE_PATH + '/trello-logo.svg';
	}
	else if (editorUi.mode == App.MODE_BROWSER)
	{
		logo.src = IMAGE_PATH + '/osa_database.png';
	}
	else
	{
		logo.src = IMAGE_PATH + '/osa_drive-harddisk.png';
	}

	if (!compact && !smallScreen && showName)
	{
		header.appendChild(logo);
	}
	
	if (showName)
	{
		mxUtils.write(header, (smallScreen? mxResources.get('name') : ((editorUi.mode == null || editorUi.mode == App.MODE_GOOGLE ||
				editorUi.mode == App.MODE_BROWSER) ? mxResources.get('diagramName') : mxResources.get('filename'))) + ':');
	}
	
	var ext = '.drawio';
	
	if (editorUi.mode == App.MODE_GOOGLE && editorUi.drive != null)
	{
		ext = editorUi.drive.extension;
	}
	else if (editorUi.mode == App.MODE_DROPBOX && editorUi.dropbox != null)
	{
		ext = editorUi.dropbox.extension;
	}
	else if (editorUi.mode == App.MODE_ONEDRIVE && editorUi.oneDrive != null)
	{
		ext = editorUi.oneDrive.extension;
	}
	else if (editorUi.mode == App.MODE_GITHUB && editorUi.gitHub != null)
	{
		ext = editorUi.gitHub.extension;
	}
	else if (editorUi.mode == App.MODE_GITLAB && editorUi.gitLab != null)
	{
		ext = editorUi.gitLab.extension;
	}
	else if (editorUi.mode == App.MODE_TRELLO && editorUi.trello != null)
	{
		ext = editorUi.trello.extension;
	}
	
	var nameInput = document.createElement('input');
	nameInput.setAttribute('value', editorUi.defaultFilename + ext);
	nameInput.style.marginLeft = '10px';
	nameInput.style.width = (compact || smallScreen) ? '144px' : '244px';
	
	this.init = function()
	{
		if (showName)
		{
			Editor.selectFilename(nameInput);
		}
		
		if (div.parentNode != null && div.parentNode.parentNode != null)
		{
			mxEvent.addGestureListeners(div.parentNode.parentNode, mxUtils.bind(this, function(evt)
			{
				if (editorUi.sidebar != null)
				{
					editorUi.sidebar.hideTooltip();
				}
			}), null, null);
		}
	};
	
	// Adds filetype dropdown
	if (showName)
	{
		header.appendChild(nameInput);

		if (withoutType)
		{
			nameInput.style.width = (compact || smallScreen) ? '350px' : '450px';
		}
		else
		{
			if (editorUi.editor.diagramFileTypes != null)
			{
				var typeSelect = FilenameDialog.createFileTypes(editorUi, nameInput, editorUi.editor.diagramFileTypes);
				typeSelect.style.marginLeft = '6px';
				typeSelect.style.width = (compact || smallScreen) ? '80px' : '180px';
				header.appendChild(typeSelect);
			}
		}
	}

	var hasTabs = false;
	var i0 = 0;
	
	// Dynamic loading
	function addTemplates(smallSize)
	{
		//smallSize: Reduce template button size to fit 4 in a row
		if (smallSize != null)
		{
			w = h = smallSize? 135 : 140;
		}
		
		var first = true;
		
		//TODO support paging of external templates
		if (templates != null)
		{
			while (i0 < templates.length && (first || mxUtils.mod(i0, 30) != 0))
			{
				var tmp = templates[i0++];
				var btn = addButton(tmp.url, tmp.libs, tmp.title, tmp.tooltip? tmp.tooltip : tmp.title,
					tmp.select, tmp.imgUrl, tmp.info, tmp.onClick, tmp.preview, tmp.noImg, tmp.clibs,
					tmp.type);
				
				if (first)
				{
					btn.click();
				}
				
				first = false;
			}
		}		
	};
	
	var spinner = new Spinner({
		lines: 12, // The number of lines to draw
		length: 10, // The length of each line
		width: 5, // The line thickness
		radius: 10, // The radius of the inner circle
		rotate: 0, // The rotation offset
		color: 'light-dark(#000000, #C0C0C0)', // #rgb or #rrggbb
		speed: 1.5, // Rounds per second
		trail: 60, // Afterglow percentage
		shadow: false, // Whether to render a shadow
		hwaccel: false, // Whether to use hardware acceleration
		top: '40%',
		zIndex: 2e9 // The z-index (defaults to 2000000000)
	});
	
	var createButton = mxUtils.button(createButtonLabel || mxResources.get('create'), function()
	{
		createButton.setAttribute('disabled', 'disabled');
		create();
		createButton.removeAttribute('disabled');
	});
	
	createButton.className = 'geBtn gePrimaryBtn';

	var magnifyImage = document.createElement('img');
	magnifyImage.setAttribute('src', Editor.magnifyImage);
	magnifyImage.setAttribute('title', mxResources.get('preview'));
	magnifyImage.className = 'geButton geRoundButton';
		
	// Shows a tooltip with the rendered template
	var loading = false;
	var extImg = null;
	var wasVisible = false;
	
	function showTooltip(xml, x, y, elt, title, url)
	{
		// Checks if dialog still visible
		if (xml != null && mxUtils.isAncestorNode(document.body, elt))
		{
			var doc = mxUtils.parseXml(xml);
			var tempNode = Editor.parseDiagramNode(doc.documentElement, null, true);
			var codec = new mxCodec(tempNode.ownerDocument);
			var model = new mxGraphModel();
			codec.decode(tempNode, model);
			var cells = model.root.children;
			
			var ww = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
			var wh = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
			
			// TODO: Use maxscreensize
			editorUi.sidebar.createTooltip(elt, cells, Math.min(ww - 120, 1000), Math.min(wh - 120, 800),
				(title != null) ? mxResources.get(title) : null,
				true, new mxPoint(x, y), true, function()
				{
					wasVisible = editorUi.sidebar.tooltip != null &&
						editorUi.sidebar.tooltip.style.display != 'none';

					if (url != null)
					{
						selectElement(elt, null, null, url, infoObj, clibs);
					}
				}, true, false);
		}
	};

	if (recentDocsCallback || searchDocsCallback)
	{
		var tabsEl = [];
		var oldTemplates = null, origCategories = null, origCustomCatCount = null;
		
		var setActiveTab = function(index)
		{
			createButton.setAttribute('disabled', 'disabled');
			
			for (var i = 0; i < tabsEl.length; i++)
			{
				if (i == index)
					tabsEl[i].className = 'geBtn gePrimaryBtn';
				else
					tabsEl[i].className = 'geBtn';
			}
		}
		
		hasTabs = true;
		var tabs = document.createElement('div');
		tabs.style.whiteSpace = 'nowrap';
		tabs.style.height = '30px';
		outer.appendChild(tabs);
		
		var templatesTab = mxUtils.button(mxResources.get('Templates', null, 'Templates'), function()
		{
			list.style.display = '';
			searchBox.style.display = '';
			div.style.left = '160px';
			setActiveTab(0);

			div.scrollTop = 0;
			div.innerText = '';
			i0 = 0;
			
			if (oldTemplates != templates)
			{
				templates = oldTemplates;
				categories = origCategories;
				customCatCount = origCustomCatCount;
				list.innerText = '';
				initUi();	
				oldTemplates = null;
			}
		});
		
		tabsEl.push(templatesTab);
		tabs.appendChild(templatesTab);
		
		var getExtTemplates = function(isSearch)
		{
			list.style.display = 'none';
			searchBox.style.display = 'none';
			div.style.left = '30px';				
			
			setActiveTab(isSearch? -1 : 1); //deselect all of them if isSearch 
			
			if (oldTemplates == null) 
			{
				oldTemplates = templates;
			}
			
			div.scrollTop = 0;
			div.innerText = '';
			spinner.spin(div);

			var callback2 = function(docList, errorMsg, searchImportCats) 
			{
				i0 = 0;
				spinner.stop();
				templates = docList;
				searchImportCats = searchImportCats || {};
				var importListsCount = 0;
				
				for (var cat in searchImportCats)
				{
					importListsCount += searchImportCats[cat].length;
				}
				
				if (errorMsg)
				{
					div.innerText = errorMsg;
				}
				else if (docList.length == 0 && importListsCount == 0)
				{
					div.innerText = mxResources.get('noDiagrams', null, 'No Diagrams Found');
				}
				else
				{
					div.innerText = '';
					
					if (importListsCount > 0)
					{
						list.style.display = '';
						div.style.left = '160px';
						list.innerText = '';

						customCatCount = 0;
						categories = {'draw.io': docList};
						
						for (var cat in searchImportCats)
						{	
							categories[cat] = searchImportCats[cat];
						}
						
						initUi();
					}
					else
					{
						addTemplates(true);
					}
				}
			}
			
			if (isSearch)
			{
				searchDocsCallback(searchInput.value, callback2);
			}
			else
			{
				recentDocsCallback(callback2);
			}
		}
		
		if (recentDocsCallback)
		{
			var recentTab = mxUtils.button(mxResources.get('Recent', null, 'Recent'), function()
			{
				getExtTemplates();
			});

			tabs.appendChild(recentTab);
			tabsEl.push(recentTab);
		}
		
		if (searchDocsCallback)
		{
			var searchTab = document.createElement('span');
			searchTab.style.marginLeft = '10px';
			searchTab.innerText = mxResources.get('search') + ':';
			tabs.appendChild(searchTab);

			var searchInput = document.createElement('input');
			searchInput.style.marginRight = '10px';
			searchInput.style.marginLeft = '10px';
			searchInput.style.width = '220px';

			mxEvent.addListener(searchInput, 'keypress', function(e)
			{
				if (e.keyCode == 13)
				{
					getExtTemplates(true);
				}
			});

			tabs.appendChild(searchInput);

			var searchBtn = mxUtils.button(mxResources.get('search'), function()
			{
				getExtTemplates(true);
			});
					
			searchBtn.className = 'geBtn';

			tabs.appendChild(searchBtn);
		}
		
		setActiveTab(0);
	}
	
	var templateLibs = null;
	var templateClibs = null;
	var templateXml = null;
	var selectedElt = null;
	var templateExtUrl = null;
	var templateRealUrl = null;
	var templateInfoObj = null;
	var lastAiXml = null;
	var lastAiTitle = null;

	function create()
	{
		if (selectedElt == generateElt && templateXml == null &&
			generateButton != null && generateInput != null)
		{
			if (callback && editorUi.spinner.spin(document.body,
				mxResources.get('generate') + ' \''+
					generateInput.value + '\''))
			{
				insertWasPressed = true;
				editorUi.hideDialog();
			}

			generateButton.click();
		}
		else
		{
			if (insertWasPressed)
			{
				editorUi.spinner.stop();
			}

			if (templateExtUrl && openExtDocCallback != null)
			{
				if (!showName && !insertWasPressed)
				{
					editorUi.hideDialog();
				}
				
				openExtDocCallback(templateExtUrl, templateInfoObj, nameInput.value);
			}
			else if (callback)
			{
				if (!showName && !insertWasPressed)
				{
					editorUi.hideDialog();
				}

				callback(templateXml, nameInput.value, templateRealUrl, templateLibs);
			}
			else
			{
				var title = nameInput.value;
					
				if (title != null && title.length > 0)
				{
					function doSave(mode, folderId, filename)
					{
						editorUi.createFile(filename, templateXml, (templateLibs != null &&
							templateLibs.length > 0) ? templateLibs : null, mode, function()
						{
							if (!insertWasPressed)
							{
								editorUi.hideDialog();
							}
						}, null, folderId, null, (templateClibs != null &&
							templateClibs.length > 0) ? templateClibs : null);
					};

					if (editorUi.mode == App.MODE_GOOGLE || editorUi.mode == App.MODE_ONEDRIVE)
					{
						var dlg = new SaveDialog(editorUi, title, mxUtils.bind(this, function(input, mode, folderId)
						{
							doSave(mode, folderId, input.value);
						}), null, null, null, null, editorUi.mode);

						editorUi.showDialog(dlg.container, 420, 150, true, false);
						dlg.init();
					}
					else
					{
						editorUi.pickFolder(editorUi.mode, function(folderId)
						{
							doSave(editorUi.mode, folderId, title);
						}, editorUi.mode != App.MODE_GOOGLE ||
							editorUi.stateArg == null ||
							editorUi.stateArg.folderId == null);
					}
				}
			}
		}
	};
	
	var div = document.createElement('div');
	div.style.border = '1px solid #d3d3d3';
	div.style.position = 'absolute';
	div.style.left = '160px';
	div.style.right = '34px';
	var divTop = (showName) ? 72 : 40;
	divTop += hasTabs? 30 : 0;
	div.style.top = divTop + 'px';
	div.style.bottom = '68px';
	div.style.margin = '6px 0 0 -1px';
	div.style.padding = '6px';
	div.style.overflow = 'auto';
	
	var searchBox = document.createElement('div');
	searchBox.style.cssText = 'position:absolute;left:30px;width:128px;top:' + divTop +
		'px;height:22px;margin-top: 6px;white-space: nowrap;';
	var tmplSearchInput = document.createElement('input');
	tmplSearchInput.style.cssText = 'width:105px;height:16px;border:1px solid #d3d3d3;' +
		'padding: 3px 20px 3px 3px;font-size: 12px;border-radius:0px;';
	tmplSearchInput.setAttribute('placeholder', mxResources.get('search'));
	tmplSearchInput.setAttribute('type', 'text');
	searchBox.appendChild(tmplSearchInput);
	
	var cross = document.createElement('img');
	cross.setAttribute('src', Editor.magnifyImage);
	cross.setAttribute('title', mxResources.get('search'));
	cross.className = 'geAdaptiveAsset';
	cross.style.position = 'relative';
	cross.style.cursor = 'pointer';
	cross.style.outline = 'none';
	cross.style.opacity = '0.5';
	cross.style.height = '16px';
	cross.style.left = '-20px';
	cross.style.top = '4px';
	searchBox.appendChild(cross);
	
	mxEvent.addListener(cross, 'click', function()
	{
		if (cross.getAttribute('src') != Editor.magnifyImage)
		{
			cross.setAttribute('src', Editor.magnifyImage);
			cross.setAttribute('title', mxResources.get('search'));
			tmplSearchInput.value = '';
			resetTemplates();
		}

		tmplSearchInput.focus();
	});
	
	mxEvent.addListener(tmplSearchInput, 'keydown', mxUtils.bind(this, function(evt)
	{
		if (evt.keyCode == 13 /* Enter */)
		{
			filterTemplates();
			mxEvent.consume(evt);
		}
	}));
	
	mxEvent.addListener(tmplSearchInput, 'keyup', mxUtils.bind(this, function(evt)
	{
		if (tmplSearchInput.value == '')
		{
			cross.setAttribute('src', Editor.magnifyImage);
			cross.setAttribute('title', mxResources.get('search'));
		}
		else
		{
			cross.setAttribute('src', Editor.crossImage);
			cross.setAttribute('title', mxResources.get('reset'));
		}
	}));

	divTop += 23;

	var list = document.createElement('div');
	list.style.cssText = 'position:absolute;left:30px;width:128px;top:' + divTop +
		'px;bottom:68px;margin-top:6px;overflow:auto;border:1px solid #d3d3d3;';
	
	mxEvent.addListener(div, 'scroll', function()
	{
		if (editorUi.sidebar != null)
		{
			editorUi.sidebar.hideTooltip();
		}
	});
	
	var w = 140;
	var h = w;
	var generateElt = null;
	var generateBackground = 'url(' + Editor.thinSparklesImage + ')';
	
	var generateForm = document.createElement('div');
	generateForm.className = 'geGenerateDiagramForm';
	generateForm.style.position = 'absolute';
	generateForm.style.width = '100%';
	generateForm.style.height = '100%';

	var generatePreview = document.createElement('div');
	generatePreview.className = 'geTemplatePreview geAdaptiveAsset';
	generatePreview.style.backgroundImage = generateBackground

	function selectElement(elt, xml, libs, extUrl, infoObj, clibs, realUrl)
	{
		if (selectedElt != elt)
		{
			if (selectedElt != null)
			{
				selectedElt.classList.remove('geTemplateSelected');
				
				if (selectedElt == generateElt)
				{
					generateForm.style.display = 'none';
					generatePreview.style.display = '';
					editGenerate.style.visibility = 'hidden';
					magnifyGenerate.style.visibility = (lastAiXml != null) ? 'visible' : 'hidden';
				}
			}

			if (elt == generateElt)
			{
				xml = lastAiXml;
				
				if (xml != null)
				{
					magnifyGenerate.style.visibility = 'visible';
					magnifyGenerate.style.visibility = (lastAiXml != null) ? 'visible' : 'hidden';
					editGenerate.style.visibility = 'visible';
				}
				else
				{
					generateForm.style.display = '';
					editGenerate.style.visibility = 'hidden';
					magnifyGenerate.style.visibility = 'hidden';
				}
			}
			
			createButton.removeAttribute('disabled');
			templateXml = xml;
			templateLibs = libs;
			templateClibs = clibs;
			selectedElt = elt;
			templateExtUrl = extUrl;
			templateRealUrl = realUrl;
			templateInfoObj = infoObj;

			selectedElt.classList.add('geTemplateSelected');
			
			return true;
		}
		else
		{
			return false;
		}
	};

	var generateInput = document.createElement('textarea');
	generateInput.setAttribute('placeholder', mxResources.get('describeYourDiagram'));
	generateInput.className = 'geGenerateDiagramDescription';

	var generateButton = document.createElement('button');
	generateButton.className = 'geBtn gePrimaryBtn geGenerateDiagramButton';
	generateButton.setAttribute('disabled', 'disabled');
	generateButton.setAttribute('title', mxResources.get('ok'));
	mxUtils.write(generateButton, mxResources.get('ok'));

	var magnifyGenerate = magnifyImage.cloneNode(true);
	magnifyGenerate.style.display = 'none';
	var generatePreviewWasVisible = false;

	var mouseDownHandler = function(evt)
	{
		generatePreviewWasVisible = editorUi.sidebar.tooltip != null &&
			editorUi.sidebar.tooltip.style.display != 'none';
	};

	var mouseUpHandler = function(evt)
	{
		if (!generatePreviewWasVisible && lastAiXml != null)
		{
			var previewXml = '<mxfile><diagram>' + Graph.compress(lastAiXml) + '</diagram></mxfile>';
			showTooltip(previewXml, mxEvent.getClientX(evt),
				mxEvent.getClientY(evt), generateElt,
				lastAiTitle);
		}
	};
	
	mxEvent.addGestureListeners(magnifyGenerate, mouseDownHandler, null, mouseUpHandler);
	
	var editGenerate = magnifyImage.cloneNode(true);
	editGenerate.setAttribute('src', Editor.editImage);
	editGenerate.setAttribute('title', mxResources.get('edit'));
	editGenerate.style.visibility = 'hidden';
	editGenerate.style.left = '0px';

	var helpGenerate = magnifyImage.cloneNode(true);
	helpGenerate.setAttribute('src', Editor.helpImage);
	helpGenerate.setAttribute('title', mxResources.get('help'));

	mxEvent.addListener(helpGenerate, 'click', function(evt)
	{
		editorUi.openLink('https://www.drawio.com/blog/write-query-generate-diagram');
		mxEvent.consume(evt);
	});
	
	generateForm.appendChild(generateInput);
	generateForm.appendChild(generateButton);
	generateForm.appendChild(helpGenerate);
	generateForm.style.display = 'none';

	function createGenerate()
	{
		generateForm.style.display = '';
		generatePreview.style.display = 'none';
		editGenerate.style.visibility = 'hidden';
		magnifyGenerate.style.visibility = 'hidden';
		generateInput.focus();

		if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
		{
			generateInput.select();
		}
		else
		{
			document.execCommand('selectAll', false, null);
		}
	};

	mxEvent.addListener(editGenerate, 'click', createGenerate);

	function updateGenerateButtonState()
	{
		if (generateInput.value != '')
		{
			generateButton.removeAttribute('disabled');
		}
		else
		{
			generateButton.setAttribute('disabled', 'disabled');
		}
	};

	mxEvent.addListener(generateInput, 'input', updateGenerateButtonState);

	function stopInput()
	{
		generatePreview.style.backgroundImage = generateBackground;
		generatePreview.style.backgroundSize = 'contain';
		generateForm.style.display = 'none';
		generatePreview.style.display = '';
		editGenerate.style.visibility = 'visible';
		magnifyGenerate.style.visibility = 'visible';
	};

	var generatingDiagram = false;

	function generateDiagram(cancel)
	{
		if (generatingDiagram)
		{
			return;
		}

		generatingDiagram = true;
		var desc = mxUtils.trim(generateInput.value);

		if (!cancel && desc != '')
		{
			generatePreview.style.backgroundImage = 'url(' + Editor.spinImage + ')';
			generatePreview.style.backgroundSize = '12px 12px';
			generatePreview.style.display = '';
			generateForm.style.display = 'none';

			editorUi.generateOpenAiMermaidDiagram(desc, function(xml)
			{
				generatingDiagram = false;

				if (selectedElt == generateElt && generateForm.style.display == 'none')
				{
					generateBackground = 'url(' + Editor.createSvgDataUri(
						mxUtils.getXml(editorUi.getSvgForXml(xml))) + ')';
					generateElt.setAttribute('title', desc);
					magnifyGenerate.style.display = '';
					templateXml = xml;
					lastAiXml = xml;
					lastAiTitle = desc;
					stopInput();

					if (insertWasPressed)
					{
						create();
					}
				}
			}, mxUtils.bind(this, function(e)
			{
				generatingDiagram = false;

				if (selectedElt == generateElt)
				{
					generateForm.style.display = '';
					generatePreview.style.display = 'none';
					editGenerate.style.visibility = 'hidden';
					magnifyGenerate.style.visibility = 'hidden';
					editorUi.handleError(e);
				}
			}), true);
		}
		else if (lastAiTitle != null)
		{
			generateInput.value = lastAiTitle;
			stopInput();
		}
	};

	mxEvent.addListener(generateButton, 'click', function()
	{
		generateDiagram();
	});	

	mxEvent.addListener(generateInput, 'keydown', function(evt)
	{
		if (evt.keyCode == 13 && !mxEvent.isShiftDown(evt))
		{
			generateDiagram();
			mxEvent.consume(evt);
		}
		else if (evt.keyCode == 27)
		{
			generateDiagram(true);
			mxEvent.consume(evt);
		}
	});

	function addButton(url, libs, title, tooltip, select, imgUrl, infoObj, onClick, preview, noImg, clibs, templateType)
	{
		var elt = null;

		if (templateType != 'generative' || generateElt == null)
		{
			elt = document.createElement('div');
			elt.className = 'geTemplate';
			var xmlData = null, realUrl = url;
			
			if (title != null)
			{
				elt.setAttribute('title', mxResources.get(title, null, title));
			}
			else if (tooltip != null && tooltip.length > 0)
			{
				elt.setAttribute('title', tooltip);
			}
		}
		else
		{
			elt = generateElt;
		}
			
		function loadXmlData(url, callback)
		{
			if (xmlData == null)
			{
				realUrl = url;
		
				if (/^https?:\/\//.test(realUrl))
				{
					realUrl = editorUi.editor.isCorsEnabledForUrl(realUrl) ? realUrl :
						PROXY_URL + '?url=' + encodeURIComponent(realUrl);
				}
				else
				{
					realUrl = TEMPLATE_PATH + '/' + realUrl;
				}
				
				mxUtils.get(realUrl, mxUtils.bind(this, function(req)
				{
					if (req.getStatus() >= 200 && req.getStatus() <= 299)
					{
						xmlData = req.getText();
						callback(xmlData, realUrl);
					}
					else
					{
						callback(xmlData, realUrl);	
					}				
				}));
			}
			else
			{
				callback(xmlData, realUrl);
			}
		};

		function loadTooltip(evt, tooltipTitle)
		{
			if (url != null && !loading && editorUi.sidebar.currentElt != elt)
			{
				editorUi.sidebar.hideTooltip();
				
				if (extImg != null)
				{
					// Create a diagram with the image to use the same code
					// Note: Without compression it doesn't work for some reason. Find out why later
					var xml = '<mxfile><diagram>' + Graph.compress('<mxGraphModel><root><mxCell id="0"/><mxCell id="1" parent="0"/>' +
						'<mxCell id="2" value="" style="shape=image;image=' + extImg.src + ';imageAspect=1;" parent="1" vertex="1">' +
						'<mxGeometry width="' + extImg.naturalWidth + '" height="' + extImg.naturalHeight + '" as="geometry" />' +
						'</mxCell></root></mxGraphModel>') + '</diagram></mxfile>';
					showTooltip(xml, mxEvent.getClientX(evt), mxEvent.getClientY(evt), title, url);
					return;
				}
				
				editorUi.sidebar.currentElt = elt;
				loading = true;
				
				loadXmlData(url, function(xml)
				{
					if (loading && editorUi.sidebar.currentElt == elt)
					{
						try
						{
							showTooltip(xml, mxEvent.getClientX(evt),
								mxEvent.getClientY(evt), elt,
								tooltipTitle);
						}
						catch (e)
						{
							editorUi.sidebar.currentElt = null;
							editorUi.handleError(e);
						}
					}
					
					loading = false;
				});
			}
			else
			{
				editorUi.sidebar.hideTooltip();
			}
		};

		var span = document.createElement('span');
		span.className = 'geAdaptiveAsset';
		mxUtils.write(span, mxResources.get(title, null, title));

		if (imgUrl != null)
		{
			elt.style.display = 'inline-flex';
			elt.style.justifyContent = 'center';
			elt.style.alignItems = 'center';
			var img = document.createElement('img');
			img.setAttribute('src', imgUrl);
			img.setAttribute('alt', tooltip);
			img.style.maxWidth = w + 'px';
			img.style.maxHeight = h + 'px';
			extImg = img;
			
			var fallbackImgUrl = imgUrl.replace('.drawio.xml', '').replace('.drawio', '').replace('.xml', '');
			elt.appendChild(img);
			
			img.onerror = function()
			{
				if (this.src != fallbackImgUrl)
				{
					this.src = fallbackImgUrl;
				}
				else
				{
					this.src = Editor.errorImage;
					this.onerror = null;
				}
			};
			
			mxEvent.addGestureListeners(elt, mxUtils.bind(this, function(evt)
			{
				selectElement(elt, null, null, url, infoObj, clibs);
			}), null, null);
			
			mxEvent.addListener(elt, 'dblclick', function(evt)
			{
				create();
				mxEvent.consume(evt);
			});
		}
		else if (!noImg && url != null && url.length > 0)
		{
			var png = preview || (TEMPLATE_PATH + '/' + url.substring(0, url.length - 4) + '.png');
			var preview = generatePreview.cloneNode(false);
			preview.style.backgroundImage = 'url(' + png + ')';
			preview.style.display = '';
			elt.appendChild(preview);
			
			if (title != null)
			{
				elt.appendChild(span);
			}
			
			function activate(doCreate)
			{
				if (spinner.spin(div))
				{
					loadXmlData(url, function(xml, realUrl)
					{
						spinner.stop();
						
						if (xml != null)
						{
							selectElement(elt, xml, libs, null, null, clibs, realUrl);
							
							if (doCreate)
							{
								create();
							}
						}
					});
				}
			};
			
			mxEvent.addGestureListeners(elt, mxUtils.bind(this, function(evt)
			{
				activate();
			}), null, null);

			mxEvent.addListener(elt, 'dblclick', function(evt)
			{
				activate(true);
				mxEvent.consume(evt);
			});
		}
		else
		{
			elt.appendChild(span);

			if (templateType == 'generative' && generateElt == null)
			{
				elt.appendChild(generatePreview);
				elt.appendChild(generateForm);
				elt.appendChild(magnifyGenerate);
				elt.appendChild(editGenerate);
				generateElt = elt;
			}

			if (select)
			{
				selectElement(elt);
			}

			if (onClick != null)
			{
				mxEvent.addGestureListeners(elt, null, null, mxUtils.bind(this, function(evt)
				{
					selectElement(elt, null, null, url, infoObj);
				}));
				
				mxEvent.addListener(elt, 'click', onClick);
			}
			else
			{
				mxEvent.addListener(elt, 'click', function(evt)
				{
					if (selectElement(elt, null, null, url, infoObj) && templateType == 'generative')
					{
						if (lastAiXml == null)
						{
							createGenerate();
						}
					}
				});

				mxEvent.addListener(elt, 'dblclick', function(evt)
				{
					if (templateType != 'generative')
					{
						create();
					}
					else if (generateForm.style.display == 'none')
					{
						createGenerate();
					}
					
					mxEvent.consume(evt);
				});
			}
		}
		
		// Adds preview button
		if (url != null)
		{
			var magnify = magnifyImage.cloneNode(true);
			elt.appendChild(magnify);
			
			mxEvent.addGestureListeners(magnify, mxUtils.bind(this, function(evt)
			{
				wasVisible = editorUi.sidebar.currentElt == elt;
			}), null, null);
			
			mxEvent.addListener(magnify, 'click', mxUtils.bind(this, function(evt)
			{
				if (!wasVisible)
				{
					loadTooltip(evt, (title != null) ? title : tooltip);
				}
				
				mxEvent.consume(evt);
			}));
		}

		div.appendChild(elt);
		return elt;
	};

	var categories = {}, subCategories = {}, customCats = {};
	var customCatCount = 0, firstInitUi = true;
	var currentEntry = null, lastEntry = null;

	// Adds local basic templates
	categories['basic'] = noBlank? [] : [{title: 'blankDiagram'}];
	var templates = categories['basic'];

	if (Editor.enableAi &&
		editorUi.isExternalDataComms() &&
		editorUi.getServiceName() == 'draw.io' &&
		typeof mxMermaidToDrawio !== 'undefined' &&
		window.isMermaidEnabled)
	{
		categories['basic'].push({title: 'generate', type: 'generative'});
	}
	
	function resetTemplates()
	{
		if (lastEntry != null)
		{
			lastEntry.click();
			lastEntry = null;
		}
	};
	
	function filterTemplates()
	{
		var searchTerms = tmplSearchInput.value;
		
		if (searchTerms == '')
		{
			resetTemplates();
			return;
		}
		
		if (NewDialog.tagsList[templateFile] == null)
		{
			var tagsList = {};
			
			for (var cat in categories)
			{
				if (categories[cat].content == null)
				{
					var templateList = categories[cat];
					
					for (var i = 0; i < templateList.length; i++)
					{
						var temp = templateList[i];
						
						if (temp.tags != null)
						{
							var tags = temp.tags.toLowerCase().split(';');
							
							for (var j = 0; j < tags.length; j++)
							{
								if (tagsList[tags[j]] == null)
								{
									tagsList[tags[j]] = [];
								}
								
								tagsList[tags[j]].push(temp);
							}
						}
					}
				}
			}
			
			NewDialog.tagsList[templateFile] = tagsList;
		}

		var tmp = searchTerms.toLowerCase().split(' ');
		tagsList = NewDialog.tagsList[templateFile];
		
		if (customCatCount > 0 && tagsList.__tagsList__ == null)
		{
			for (var cat in customCats)
			{
				var templateList = customCats[cat];
				
				for (var i = 0; i < templateList.length; i++)
				{
					var temp = templateList[i];
					var tags = temp.title.split(' ');
					tags.push(cat);
					
					for (var j = 0; j < tags.length; j++)
					{
						var tag = tags[j].toLowerCase();
						
						if (tagsList[tag] == null)
						{
							tagsList[tag] = [];
						}
						
						tagsList[tag].push(temp);
					}
				}				
			}
			
			tagsList.__tagsList__ = true;
		}
		
		var results = [], resMap = {}, index = 0;
		
		for (var i = 0; i < tmp.length; i++)
		{
			if (tmp[i].length > 0)
			{
				var list = tagsList[tmp[i]];
				var tmpResMap = {};
				results = [];
				
				if (list != null)
				{
					for (var j = 0; j < list.length; j++)
					{
						var temp = list[j];
						
						//ANDing terms
						if ((index == 0) == (resMap[temp.url] == null))
						{
							tmpResMap[temp.url] = true;
							results.push(temp);
						}
					}
				}
				
				resMap = tmpResMap;
				index++;
			}
		}
		
		div.scrollTop = 0;
		div.innerText = '';
		i0 = 0;
		var msgDiv = document.createElement('div');
		msgDiv.style.padding = '6px';
		msgDiv.style.whiteSpace = 'nowrap';
		msgDiv.style.overflow = 'hidden';
		msgDiv.style.textOverflow = 'ellipsis';
		var temp = mxResources.get(results.length == 0 ?
			'noResultsFor' : 'resultsFor', [searchTerms]);
		msgDiv.setAttribute('title', temp);
		mxUtils.write(msgDiv, temp);
		div.appendChild(msgDiv);

		if (currentEntry != null && lastEntry == null)
		{
			currentEntry.style.backgroundColor = '';
			lastEntry = currentEntry;
			currentEntry = msgDiv; //To prevent NPE later
		}

		templates = results;
		oldTemplates = null;
		addTemplates(false);
	};
	
	function initUi()
	{
		if (firstInitUi)
		{
			firstInitUi = false;
			
			mxEvent.addListener(div, 'scroll', function(evt)
			{
				if (div.scrollTop + div.clientHeight >= div.scrollHeight)
				{
					addTemplates();
					mxEvent.consume(evt);
				}
			});
		}

		if (customCatCount > 0)
		{
			var titleCss = 'font-weight: bold;background: #f9f9f9;padding: 5px 0 5px 0;text-align: center;';
			var title = document.createElement('div');
			title.style.cssText = titleCss;
			mxUtils.write(title, mxResources.get('custom'));
			list.appendChild(title);
			
			for (var cat in customCats)
			{
				var entry = document.createElement('div');
				var label = cat;
				var templateList = customCats[cat];
				
				if (label.length > 18)
				{
					label = label.substring(0, 18) + '&hellip;';
				}
				
				entry.style.cssText = 'display:block;cursor:pointer;padding:6px;white-space:nowrap;margin-bottom:-1px;overflow:hidden;text-overflow:ellipsis;user-select:none;';
				entry.setAttribute('title', label + ' (' + templateList.length + ')');
				mxUtils.write(entry, entry.getAttribute('title'));
				
				if (itemPadding != null)
				{
					entry.style.padding = itemPadding;
				}

				list.appendChild(entry);
				
				(function(cat2, entry2)
				{
					mxEvent.addListener(entry, 'click', function()
					{
						if (currentEntry != entry2)
						{
							currentEntry.style.backgroundColor = '';
							currentEntry = entry2;
							currentEntry.style.backgroundColor = leftHighlight;
							
							div.scrollTop = 0;
							div.innerText = '';
							i0 = 0;
							
							templates = customCats[cat2];
							oldTemplates = null;
							addTemplates(false);
						}
					});
				})(cat, entry);
			}
			
			title = document.createElement('div');
			title.style.cssText = titleCss;
			mxUtils.write(title, 'draw.io');
			list.appendChild(title);
		}
		
		function getEntryTitle(cat, templateList)
		{
			var label = mxResources.get(cat, null,
				cat.substring(0, 1).toUpperCase() +
				cat.substring(1));
			
			if (label.length > 18)
			{
				label = label.substring(0, 18) + '&hellip;';
			}
			
			return label + ((templateList != null) ?
				' (' + templateList.length + ')' : '');
		};
		
		function addEntryHandler(cat, entry, subCat)
		{
			mxEvent.addListener(entry, 'click', function()
			{
				if (currentEntry != entry)
				{
					if (currentEntry != null)
					{
						currentEntry.style.backgroundColor = '';
					}
					
					currentEntry = entry;
					currentEntry.style.backgroundColor = leftHighlight;

					div.scrollTop = 0;
					div.innerText = '';
					i0 = 0;

					if (categories[cat].content != null)
					{
						div.appendChild(categories[cat].content);
						templateXml = lastAiXml;
						templates = null;

						if (categories[cat].content.init != null)
						{
							categories[cat].content.init();
						}
					}
					else
					{
						templates = subCat? subCategories[cat][subCat] : categories[cat];
						oldTemplates = null;
						addTemplates(false);
					}
				}
			});
		};
			
		for (var cat in categories)
		{
			var templateList = null;
			var clickElem = null;

			if (categories[cat].content != null)
			{
				var entry = document.createElement(subCats? 'ul' : 'div');
				var title = getEntryTitle(cat);

				entry.style.cssText = 'display:block;cursor:pointer;padding:6px;white-space:nowrap;margin-bottom:-1px;overflow:hidden;text-overflow:ellipsis;user-select:none;transition: all 0.5s;';
				entry.setAttribute('title', title);
				mxUtils.write(entry, title);

				list.appendChild(entry);
				clickElem = entry;
			}
			else
			{
				var subCats = subCategories[cat];
				var entry = document.createElement(subCats? 'ul' : 'div');
				var clickElem = entry;
				templateList = categories[cat];
				var entryTitle = getEntryTitle(cat, templateList);
				
				if (subCats != null)
				{
					var entryLi = document.createElement('li');
					var entryDiv = document.createElement('div');
					entryDiv.className = 'geTempTreeCaret';
					entryDiv.setAttribute('title', entryTitle);
					mxUtils.write(entryDiv, entryTitle);
					clickElem = entryDiv;
					entryLi.appendChild(entryDiv);
					//We support one level deep only
					var subUl = document.createElement('ul');
					subUl.className = 'geTempTreeNested';
					subUl.style.visibility = 'hidden';
					
					for (var subCat in subCats)
					{
						var subLi = document.createElement('li');
						var subTitle = getEntryTitle(subCat, subCats[subCat]);
						subLi.setAttribute('title', subTitle);
						mxUtils.write(subLi, subTitle);
						addEntryHandler(cat, subLi, subCat);
						subUl.appendChild(subLi);
					}
					
					entryLi.appendChild(subUl);
					entry.className = 'geTempTree';
					entry.appendChild(entryLi);
					
					(function(subUl2, entryDiv2)
					{
						mxEvent.addListener(entryDiv2, 'click', function()
						{
							subUl2.style.visibility = 'visible';
							subUl2.classList.toggle('geTempTreeActive');
							
							if (subUl2.classList.toggle('geTempTreeNested'))
							{
								//Must hide sub elements to allow click on elements above it
								setTimeout(function()
								{
									subUl2.style.visibility = 'hidden';
								}, 550);
							}
							
							entryDiv2.classList.toggle('geTempTreeCaret-down');
						});
					})(subUl, entryDiv);
				}
				else
				{
					entry.style.cssText = 'display:block;cursor:pointer;padding:6px;white-space:nowrap;margin-bottom:-1px;overflow:hidden;text-overflow:ellipsis;user-select:none;transition: all 0.5s;';
					entry.setAttribute('title', entryTitle);
					mxUtils.write(entry, entryTitle);
				}
				
				if (itemPadding != null)
				{
					entry.style.padding = itemPadding;
				}

				list.appendChild(entry);
			}

			addEntryHandler(cat, clickElem);

			if (currentEntry == null)
			{
				clickElem.click();
			}
		}
		
		addTemplates(false);
	};

	if (!compact)
	{
		outer.appendChild(searchBox);
		outer.appendChild(list);
		outer.appendChild(div);
		var indexLoaded = false;
		var realUrl = templateFile;
		
		if (/^https?:\/\//.test(realUrl) && !editorUi.editor.isCorsEnabledForUrl(realUrl))
		{
			realUrl = PROXY_URL + '?url=' + encodeURIComponent(realUrl);
		}
		
		function loadDrawioTemplates()
		{
			mxUtils.get(realUrl, function(req)
			{
				// Workaround for index loaded 3 times in iOS offline mode
				if (!indexLoaded)
				{
					indexLoaded = true;
					var tmpDoc = req.getXml();
					var node = tmpDoc.documentElement.firstChild;
					var clibs = {};
		
					while (node != null)
					{
						if (typeof(node.getAttribute) !== 'undefined')
						{
							if (node.nodeName == 'parsererror')
							{
								if (window.console != null)
								{
									console.log('Parser error in ' +
										templateFile + ': ' +
										node.textContent);
								}
							}
							else if (node.nodeName == 'clibs')
							{
								var name = node.getAttribute('name');
								var adds = node.getElementsByTagName('add');
								var temp = [];
								
								for (var i = 0; i < adds.length; i++)
								{
									temp.push(encodeURIComponent(mxUtils.getTextContent(adds[i])));
								}
								
								if (name != null && temp.length > 0)
								{
									clibs[name] = temp.join(';');
								}
							}
							else
							{
								var url = node.getAttribute('url');
								
								if (url != null)
								{
									var category = node.getAttribute('section');
									var subCategory = node.getAttribute('subsection');
									
									if (category == null)
									{
										var slash = url.indexOf('/');
										category = url.substring(0, slash);
										
										if (subCategory == null)
										{
											var nextSlash = url.indexOf('/', slash + 1);
											
											if (nextSlash > -1)
											{
												subCategory = url.substring(slash + 1, nextSlash);
											}
										}
									}
									
									var list = categories[category];
									
									if (list == null)
									{
										list = [];
										categories[category] = list;
									}
									
									var tempLibs = node.getAttribute('clibs');
									
									if (clibs[tempLibs] != null)
									{
										tempLibs = clibs[tempLibs];
									}
									
									var tempObj = {url: node.getAttribute('url'), libs: node.getAttribute('libs'),
										title: node.getAttribute('title'), tooltip: node.getAttribute('name') || node.getAttribute('url'),
										preview: node.getAttribute('preview'), clibs: tempLibs, tags: node.getAttribute('tags')};
									list.push(tempObj);
										
									if (subCategory != null)
									{
										var subCats = subCategories[category];
										
										if (subCats == null)
										{
											subCats = {};
											subCategories[category] = subCats;
										}
										
										var subCatList = subCats[subCategory];
										
										if (subCatList == null)
										{
											subCatList = [];
											subCats[subCategory] = subCatList;
										}
										
										subCatList.push(tempObj);
									}
								}
							}
						}
						
						node = node.nextSibling;
					}
					
				
				spinner.stop();
					initUi();
				}
			});
		};
		
		spinner.spin(div);
		
		if (customTempCallback != null)
		{
			customTempCallback(function(cats, count)
			{
				customCats = cats;
				customCatCount = count;
				//Custom templates doesn't change after being loaded, so cache them here. Also, only count is overridden
				origCustomCatCount = count;

				loadDrawioTemplates();
			},
			
			loadDrawioTemplates); //In case of an error, just load draw.io templates only
		}
		else
		{
			loadDrawioTemplates();
		}
		
		//draw.io templates doesn't change after being loaded, so cache them here
		origCategories = categories;
	}
	
	mxEvent.addListener(nameInput, 'keypress', function(e)
	{
		if (editorUi.dialog.container.firstChild == outer &&
			e.keyCode == 13)
		{
			create();
		}
	});
	
	var btns = document.createElement('div');
	btns.style.marginTop = (compact) ? '4px' : '16px';
	btns.style.textAlign = 'right';
	btns.style.position = 'absolute';
	btns.style.left = '40px';
	btns.style.bottom = '24px';
	btns.style.right = '40px';
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		if (cancelCallback != null)
		{
			cancelCallback();
		}
		
		editorUi.hideDialog(true);
	});
	
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst && (!createOnly || cancelCallback != null))
	{
		btns.appendChild(cancelBtn);
	}

	if (!compact && urlParams['embed'] != '1' && !createOnly && !mxClient.IS_ANDROID &&
		!mxClient.IS_IOS && urlParams['noDevice'] != '1')
	{
		var fromTmpBtn = mxUtils.button(mxResources.get('fromTemplateUrl'), function()
		{
			var dlg = new FilenameDialog(editorUi, '', mxResources.get('create'), function(fileUrl)
			{
				if (fileUrl != null && fileUrl.length > 0)
				{
					editorUi.editor.loadUrl(editorUi.editor.getProxiedUrl(fileUrl), function(data)
					{
						templateXml = data;
						templateLibs = null;
						templateRealURl = fileUrl;

						editorUi.hideDialog();
						create();
					}, function(err)
					{
						editorUi.handleError(err);
					});
				}
			}, mxResources.get('url'), null, null, null, false);
			editorUi.showDialog(dlg.container, 300, 80, true, true);
			dlg.init();
		});
		
		fromTmpBtn.className = 'geBtn';
		btns.appendChild(fromTmpBtn);
	}
	
	if (Graph.fileSupport && showImport)
	{
		var importBtn = mxUtils.button(mxResources.get('import'), function()
		{
			if (editorUi.newDlgFileInputElt == null) 
			{
				var fileInput = document.createElement('input');
				fileInput.setAttribute('multiple', 'multiple');
				fileInput.setAttribute('type', 'file');
				
				mxEvent.addListener(fileInput, 'change', function(evt)
				{
					editorUi.openFiles(fileInput.files, true);
					fileInput.value = '';
				});
				
				fileInput.style.display = 'none';
				document.body.appendChild(fileInput);
				editorUi.newDlgFileInputElt = fileInput;
			}
			
			editorUi.newDlgFileInputElt.click();
		});
				
		importBtn.className = 'geBtn';
		btns.appendChild(importBtn);
	}
	
	btns.appendChild(createButton);
	
	if (!editorUi.editor.cancelFirst && callback == null && (!createOnly || cancelCallback != null))
	{
		btns.appendChild(cancelBtn);
	}
	
	outer.appendChild(btns);
	
	this.container = outer;
};

NewDialog.tagsList = {};

/**
 * 
 */
var SaveDialog = function(editorUi, title, saveFn, disabledModes, data, mimeType,
	base64Encoded, defaultMode, folderPickerMode, enabledModes, saveBtnLabel)
{
	var div = document.createElement('div');
	div.style.display = 'flex';
	div.style.flexWrap = 'wrap';
	div.style.whiteSpace = 'nowrap';

	var table = document.createElement('div');
	table.style.display = 'grid';
	table.style.gap = '5px 8px';
	table.style.gridAutoRows = 'auto auto 44px';
	table.style.gridAutoColumns = '0fr minmax(0,1fr)';
	table.style.width = '100%';

	var preview = null;
	var copyBtn = null;

	// Disables SVG preview if SVG is not supported in browser
	if (data != null && mimeType != null && (mimeType.substring(0, 6) == 'image/' &&
		(mimeType.substring(0, 9) != 'image/svg' || mxClient.IS_SVG)))
	{
		table.style.display = 'inline-grid';
		table.style.flexBasis = '75%';

		preview = document.createElement('div');
		preview.style.display = 'inline-block';
		preview.style.height = 'auto';
		preview.style.maxWidth = '25%';
		preview.style.margin = 'auto';

		var img = document.createElement('img');
		var temp = (base64Encoded) ? data : btoa(unescape(encodeURIComponent(data)));
		var dataUri = 'data:' + mimeType + ';base64,' + temp;
		img.setAttribute('src', dataUri);
		img.style.boxSizing = 'border-box';
		img.style.maxHeight = '50px';
		img.style.maxWidth = '100%';
		img.style.paddingLeft = '10px';
		preview.appendChild(img);

		if ((mimeType == 'image/png' || mimeType == 'image/svg+xml') &&
			typeof window.ClipboardItem === 'function' &&
			navigator.clipboard != null)
		{
			copyBtn = mxUtils.button(mxResources.get('copy'), function()
			{
				editorUi.writeImageToClipboard(dataUri, null, null, mimeType,
					mxUtils.bind(this, function()
					{
						if (defaultMode == 'copy')
						{
							editorUi.hideDialog();
						}
						else
						{
							editorUi.alert(mxResources.get('copiedToClipboard'));
						}
					}),
					mxUtils.bind(this, function(e)
					{
						editorUi.handleError(e);
					}));
			}, null, 'geBtn');
		}

		if (Editor.popupsAllowed && (disabledModes == null ||
			mxUtils.indexOf(disabledModes, '_blank') < 0))
		{
			preview.setAttribute('title', mxResources.get('openInNewWindow'));
			preview.style.cursor = 'pointer';
			
			mxEvent.addGestureListeners(preview, null, null, function(evt)
			{
				if (!mxEvent.isPopupTrigger(evt))
				{
					editorUi.openInNewWindow(data, mimeType, base64Encoded);
				}
			});
		}
		else
		{
			preview.setAttribute('title', mxResources.get('preview'));
		}
	}
	
	var left = document.createElement('div');
	left.style.display = 'flex';
	left.style.padding = '1px';
	left.style.alignItems = 'center';
	left.style.justifyContent = 'flex-end';
	left.style.gridColumn = '1';
	left.style.whiteSpace = 'nowrap';

	var right = document.createElement('div');
	right.style.display = 'grid';
	right.style.padding = '1px';
	right.style.alignItems = 'center';
	right.style.gridColumn = '2';
	right.style.gridAutoColumns = 'minmax(0,1fr) auto';
	right.style.gap = '6px';
	
	mxUtils.write(left, mxResources.get('saveAs') + ':');

	var saveAsInput = document.createElement('input');
	saveAsInput.setAttribute('type', 'text');
	saveAsInput.setAttribute('value', title);
	saveAsInput.style.boxSizing = 'border-box';
	saveAsInput.style.width = '100%';
	right.appendChild(saveAsInput);

	if (folderPickerMode == null)
	{
		table.appendChild(left);
		table.appendChild(right);
	}

	var typeSelect = null;

	if (editorUi.editor.diagramFileTypes != null && mimeType == null &&
		folderPickerMode == null)
	{
		left = left.cloneNode(false);
		right = right.cloneNode(false);

		mxUtils.write(left, mxResources.get('type') + ':');
		
		typeSelect = FilenameDialog.createFileTypes(editorUi, saveAsInput,
			editorUi.editor.diagramFileTypes);
		typeSelect.style.boxSizing = 'border-box';
		typeSelect.style.width = '100%';
		right.appendChild(typeSelect);

		table.appendChild(left);
		table.appendChild(right);
	}
	
	left = left.cloneNode(false);
	right = right.cloneNode(false);

	mxUtils.write(left, mxResources.get('where') + ':');

	var storageSelect = document.createElement('select');
	storageSelect.style.textOverflow = 'ellipsis';
	storageSelect.style.gridColumn = '1';

	var localServices = ['browser', 'device', 'download', '_blank'];
	var dash = '&nbsp;&nbsp;&#8211&nbsp;&nbsp;';
	
	function addStorageEntry(mode, path, id, selected, title, entryType)
	{
		var option = null;

		if ((disabledModes == null || mxUtils.indexOf(disabledModes, mode) < 0) &&
			(folderPickerMode == null || mode == folderPickerMode) &&
			(enabledModes == null || mxUtils.indexOf(enabledModes, mode) >= 0))
		{
			title = (title != null) ? title : editorUi.getTitleForService(mode);
			var isLocal = mxUtils.indexOf(localServices, mode) >= 0;

			if (isLocal || editorUi.getServiceForName(mode) != null)
			{
				option = document.createElement('option');
				var state = '';

				if (!isLocal && editorUi.isOffline(mode == App.MODE_GOOGLE &&
					urlParams['gapi-stealth'] == '1'))
				{
					option.setAttribute('disabled', 'disabled');
					state = ' (' + mxResources.get('offline') + ')';
				}

				if (entryType == 'pick')
				{
					option.innerHTML = mxUtils.htmlEntities(title) + dash +
						mxUtils.htmlEntities(mxResources.get('pickFolder')) +
						'...' + state;
					option.setAttribute('value', 'pickFolder-' + mode);
					option.setAttribute('title', title + ' - ' +
						mxResources.get('pickFolder') + '...');
				}
				else
				{
					var entryId = mode + ((id != null) ? ('-' + id) : '');
					var entry = entries[entryId];

					if (entry != null && entry.option != null)
					{
						entry.option.parentNode.removeChild(entry.option);
					}

					var shortPath = null;

					if (path != null)
					{
						if (path.charAt(path.length - 1) == '/')
						{
							path = path.substring(0, path.length - 1);
						}

						if (path.charAt(0) == '/')
						{
							path = path.substring(1);
						}

						shortPath = path;

						if (mode != App.MODE_GITHUB && mode == App.MODE_GITLAB)
						{
							var idx = shortPath.lastIndexOf('/');

							if (idx >= 0)
							{
								shortPath = shortPath.substring(idx + 1);
							}
						}
						
						if (shortPath.length > 40)
						{
							shortPath = shortPath.substring(0, 20) + '...' +
								shortPath.substring(shortPath.length - 20);
						}
					}

					option.innerHTML = mxUtils.htmlEntities(title) + ((shortPath != null) ?
						dash + mxUtils.htmlEntities(shortPath) : '') + state;
					option.setAttribute('title', title + ((path != null) ? ' (' + path + ')' : '') +
						((id != null && decodeURIComponent(id) != path) ? ' [' + id + ']' : ''));
					option.setAttribute('value', entryId);
					entries[entryId] = {option: option, mode: mode, path: path, id: id};

					if (SaveDialog.lastValue == entryId && defaultMode == null &&
						!option.getAttribute('disabled') == 'disabled')
					{
						selected = true;
					}
					else if (selected == null)
					{
						if (entryType == 'root')
						{
							selected = (defaultMode == null && editorUi.mode == mode) ||
								(mode != null && mode == defaultMode);
						}
						else if (storageSelect.value.substring(0, 11) == 'pickFolder-')
						{
							selected = true;
						}
					}

					if (selected)
					{
						option.setAttribute('selected', 'selected');
					}
				}

				storageSelect.appendChild(option);
			}
		}

		return option;
	};

	var defaultValue = null;

	function pickFolder(mode)
	{
		editorUi.pickFolder(mode, function(result)
		{
			var entry = null;

			if (mode == App.MODE_GOOGLE && result.docs != null && result.docs.length > 0)
			{
				entry = {mode: mode, path: result.docs[0].name, id: result.docs[0].id};
			}
			else if (mode == App.MODE_ONEDRIVE && result.value != null && result.value.length > 0)
			{
				entry = {mode: mode, path: result.value[0].name,
					id: OneDriveFile.prototype.getIdOf(result.value[0])};
			}
			else if (mode == App.MODE_M365 && result.value != null && result.value.length > 0) {
				entry = {
					mode: mode, path: result.value[0].name,
					id: OneDriveFile.prototype.getIdOf(result.value[0])
				};
			}
			else if ((mode == App.MODE_GITHUB || mode == App.MODE_GITLAB) &&
				result != null && result.length > 0)
			{
				entry = {mode: mode, path: decodeURIComponent(result), id: result};
			}

			if (entry != null)
			{
				editorUi.addRecent(entry, 'Folders', 5);

				var option = addStorageEntry(entry.mode, entry.path, entry.id, true);
				storageSelect.innerHTML = '';
				entries = {};
				addStorageEntries();

				// Selects new entry
				var prev = storageSelect.selectedIndex;
				storageSelect.value = option.value;

				// Checks if entry exists
				// LATER: Pass value to select to addStorageEntries
				if (storageSelect.selectedIndex < 0)
				{
					storageSelect.selectedIndex = prev;
				}
			}
		}, true, true, true, true);
	};

	var entries = {};
	
	function checkExtension()
	{
		if (typeSelect != null &&  entries[storageSelect.value] != null &&
			editorUi.editor.diagramFileTypes != null &&
			editorUi.editor.diagramFileTypes[typeSelect.value].extension == 'drawio')
		{
			var ext = editorUi.getExtensionForService(entries[storageSelect.value].mode);
			var name = saveAsInput.value;

			if (ext != null && title.indexOf('.') < 0 &&
				name.indexOf('.') < 0)
			{
				saveAsInput.value = name + ext;
			}
		}
	};

	var resetBtn = mxUtils.button(mxResources.get('reset'), function()
	{
		saveAsInput.value = title;
		saveAsInput.dispatchEvent(new Event('change'));
		editorUi.resetRecent('Folders');
		storageSelect.innerHTML = '';
		storageSelect.value = '';
		pickFolderOption = null;
		entries = {};
		addStorageEntries();
	}, null, 'geBtn');

	function addStorageEntries()
	{
		var recent = editorUi.getRecent('Folders');
		var recentCount = 0;

		if (recent != null && recent.length > 0)
		{
			for (var i = 0; i < recent.length; i++)
			{
				if (addStorageEntry(recent[i].mode, recent[i].path, recent[i].id) != null)
				{
					recentCount++;
				}
			}
		}

		addStorageEntry(App.MODE_GOOGLE, mxResources.get('myDrive'),
			'root', null, null, 'root');
		addStorageEntry(App.MODE_GOOGLE, null, null, null, null, 'pick');

		if (editorUi.oneDrive != null)
		{
			addStorageEntry(App.MODE_ONEDRIVE, mxResources.get('myFiles'),
				OneDriveFile.prototype.getIdOf(editorUi.oneDrive.rootId),
				null, null, 'root');
			addStorageEntry(App.MODE_ONEDRIVE, null, null, null, null, 'pick');
		}

		if (editorUi.m365 != null)
		{
			addStorageEntry(App.MODE_M365, null, null, null, null, 'pick');
		}

		if (editorUi.dropbox != null)
		{
			addStorageEntry(App.MODE_DROPBOX, 'Apps' + editorUi.dropbox.appPath);
		}

		addStorageEntry(App.MODE_GITHUB, null, null, null, null, 'pick');
		addStorageEntry(App.MODE_GITLAB, null, null, null, null, 'pick');

		addStorageEntry(App.MODE_TRELLO);

		var allowDevice = !Editor.useLocalStorage || urlParams['storage'] == 'device' ||
			(editorUi.getCurrentFile() != null && urlParams['noDevice'] != '1');

		if (EditorUi.nativeFileSupport && allowDevice)
		{
			addStorageEntry(App.MODE_DEVICE, null, null, editorUi.mode == App.MODE_DEVICE ||
				(disabledModes != null && mxUtils.indexOf(disabledModes,
					App.MODE_BROWSER) >= 0) ? true : null);
		}
		
		if (isLocalStorage && urlParams['browser'] != '0')
		{
			addStorageEntry(App.MODE_BROWSER);
		}

		if (allowDevice)
		{
			addStorageEntry('download');
		}
		
		if (Editor.popupsAllowed)
		{
			addStorageEntry('_blank', null, null, null, mxResources.get('openInNewWindow'));
		}

		// Adds title to avoid entries that execute an action
		if (storageSelect.value.substring(0, 11) == 'pickFolder-')
		{
			var option = document.createElement('option');
			option.setAttribute('value', '');
			option.setAttribute('selected', 'selected');
			mxUtils.write(option, mxResources.get('pickFolder') + '...');
			storageSelect.insertBefore(option, storageSelect.firstChild);
		}
		
		defaultValue = storageSelect.value;
	};

	// Label is updated below
	var saveBtn = mxUtils.button('', function()
	{
		SaveDialog.lastValue = storageSelect.value;
		var entry = entries[SaveDialog.lastValue];

		if (entry != null)
		{
			saveFn(saveAsInput, entry.mode, entry.id);
		}
	}, null, 'geBtn gePrimaryBtn');

	// Handles enter key
	mxEvent.addListener(saveAsInput, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			if (defaultMode == 'copy' && copyBtn != null)
			{
				copyBtn.click();
			}
			else
			{
				saveBtn.click();
			}
		}
	});

	function storageChanged()
	{
		if (storageSelect.value.substring(0, 11) == 'pickFolder-')
		{
			var mode = storageSelect.value.substring(11);
			storageSelect.value = defaultValue;
			pickFolder(mode);
		}
		else
		{
			checkExtension();
		}

		saveBtn.innerHTML = '';
		mxUtils.write(saveBtn, (saveBtnLabel != null) ? saveBtnLabel :
			mxResources.get((storageSelect.value == 'download' ||
			storageSelect.value == '_blank' ||
			folderPickerMode != null) ?
				'ok' : 'save'));
		
		if (storageSelect.value == '')
		{
			saveBtn.setAttribute('disabled', 'disabled');
		}
		else
		{
			saveBtn.removeAttribute('disabled');
		}
	};
	
	mxEvent.addListener(storageSelect, 'change', storageChanged);
	addStorageEntries();

	right.appendChild(storageSelect);

	// Selects last entry
	if (SaveDialog.lastValue != null && entries[SaveDialog.lastValue] != null)
	{
		storageSelect.value = SaveDialog.lastValue;
	}

	storageChanged();
	table.appendChild(left);
	table.appendChild(right);
	div.appendChild(table);

	if (preview != null)
	{
		div.appendChild(preview);
	}

	var btns = document.createElement('div');
	btns.style.flexBasis = '100%';
	btns.style.textAlign = 'right';
	btns.style.marginTop = (mimeType != null) ? '16px' : '8px';

	if (!editorUi.isOffline() || mxClient.IS_CHROMEAPP)
	{
		btns.appendChild(editorUi.createHelpIcon(
			'https://www.drawio.com/doc/faq/save-file-formats'));
	}

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	}, null, 'geBtn');

	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	btns.appendChild(resetBtn);

	if (defaultMode == 'copy' && copyBtn != null)
	{
		copyBtn.className = 'geBtn gePrimaryBtn';
		saveBtn.className = 'geBtn';
		btns.appendChild(saveBtn);
		btns.appendChild(copyBtn);
	}
	else
	{
		// Copy
		if (copyBtn != null)
		{
			btns.appendChild(copyBtn);
		}

		// Save
		btns.appendChild(saveBtn);
	}

	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	div.appendChild(btns);

	this.init = function()
	{
		Editor.selectFilename(saveAsInput);
	};

	this.container = div;
};

/**
 * Constructs a dialog for creating new files from a template URL.
 * Also used for dialog choosing where to save or export resources
 */
var CreateDialog = function(editorUi, title, createFn, cancelFn, dlgTitle, btnLabel, overrideExtension, allowBrowser,
	allowTab, helpLink, showDeviceButton, rowLimit, data, mimeType, base64Encoded, hints, hideDialog)
{
	showDeviceButton = urlParams['noDevice'] == '1'? false : showDeviceButton;
	overrideExtension = (overrideExtension != null) ? overrideExtension : true;
	allowBrowser = (allowBrowser != null) ? allowBrowser : true;
	rowLimit = (rowLimit != null) ? rowLimit : 4;
	hideDialog = (hideDialog != null) ? hideDialog : true;

	var div = document.createElement('div');
	div.style.whiteSpace = 'nowrap';
	
	var showButtons = true;
	
	if (cancelFn == null)
	{
		editorUi.addLanguageMenu(div);
	}

	var h3 = document.createElement('h2');
	mxUtils.write(h3, dlgTitle || mxResources.get('create'));
	h3.style.marginTop = '0px';
	h3.style.marginBottom = '24px';
	div.appendChild(h3);

	var span = document.createElement('span');
	mxUtils.write(span, mxResources.get('filename') + ':');
	span.style.maxWidth = '106px';
	span.style.overflow = 'hidden';
	span.style.textOverflow = 'ellipsis';
	span.style.display = 'inline-block';
	div.appendChild(span);

	var nameInput = document.createElement('input');
	nameInput.setAttribute('value', title);
	nameInput.style.width = '180px';
	nameInput.style.marginLeft = '10px';
	nameInput.style.marginBottom = '20px';
	nameInput.style.maxWidth = '70%';
	
	this.init = function()
	{
		Editor.selectFilename(nameInput);
	};

	div.appendChild(nameInput);

	if (hints != null && editorUi.editor.diagramFileTypes != null)
	{
		var typeSelect = FilenameDialog.createFileTypes(editorUi, nameInput, editorUi.editor.diagramFileTypes);
		typeSelect.style.marginLeft = '6px';
		typeSelect.style.width = '90px';
		div.appendChild(typeSelect);
	}
	
	var copyBtn = null;
	
	// Disables SVG preview if SVG is not supported in browser
	if (urlParams['noDevice'] != '1' && data != null && mimeType != null && (mimeType.substring(0, 6) == 'image/' &&
		(mimeType.substring(0, 9) != 'image/svg' || mxClient.IS_SVG)))
	{
		nameInput.style.width = '160px';
		var preview = document.createElement('img');
		var temp = (base64Encoded) ? data : btoa(unescape(encodeURIComponent(data)));
		preview.setAttribute('src', 'data:' + mimeType + ';base64,' + temp);
		preview.style.position = 'absolute';
		preview.style.top = '70px';
		preview.style.right = '100px';
		preview.style.maxWidth = '120px';
		preview.style.maxHeight = '80px';
		mxUtils.setPrefixedStyle(preview.style, 'transform',
			'translate(50%,-50%)');
		div.appendChild(preview);
		
		if (!mxClient.IS_FF  && mimeType == 'image/png' && navigator.clipboard != null &&
			typeof window.ClipboardItem === 'function')
		{
			copyBtn = mxUtils.button(mxResources.get('copy'), function(evt)
			{
				var blob = editorUi.base64ToBlob(temp, 'image/png');
				var html = '<img src="' + 'data:' + mimeType + ';base64,' + temp + '">';
				var cbi = new ClipboardItem({'image/png': blob,
					'text/html': new Blob([html], {type: 'text/html'})});
				navigator.clipboard.write([cbi]).then(mxUtils.bind(this, function()
				{
					editorUi.alert(mxResources.get('copiedToClipboard'));
				}))['catch'](mxUtils.bind(this, function(e)
				{
					editorUi.handleError(e);
				}));
			});
			
			copyBtn.style.marginTop = '6px';
			copyBtn.className = 'geBtn';
		}
		
		if (allowTab && Editor.popupsAllowed)
		{
			preview.style.cursor = 'pointer';
			
			mxEvent.addGestureListeners(preview, null, null, function(evt)
			{
				if (!mxEvent.isPopupTrigger(evt))
				{
					create('_blank');
				}
			});
		}
	}
	
	mxUtils.br(div);
	
	var buttons = document.createElement('div');
	buttons.style.textAlign = 'center';
	var count = 0;

	function addLogo(img, title, mode, clientName)
	{
		var button = document.createElement('a');
		button.style.overflow = 'hidden';
		
		var logo = document.createElement('img');
		logo.src = img;
		logo.setAttribute('border', '0');
		logo.setAttribute('align', 'absmiddle');
		logo.style.width = '60px';
		logo.style.height = '60px';
		logo.style.paddingBottom = '6px';
		button.style.display = 'inline-block';
		button.className = 'geBaseButton';
		button.style.position = 'relative';
		button.style.margin = '4px';
		button.style.padding = '8px 8px 10px 8px';
		button.style.whiteSpace = 'nowrap';
		
		button.appendChild(logo);
		
		button.style.color = 'gray';
		button.style.fontSize = '11px';
		
		var label = document.createElement('div');
		button.appendChild(label);
		mxUtils.write(label, title);
		
		function initButton()
		{
			mxEvent.addListener(button, 'click', function()
			{
				// Updates extension
				change(mode);
				create(mode);
			});
		};
		
		// Supports lazy loading
		if (clientName != null && editorUi[clientName] == null)
		{
			logo.style.visibility = 'hidden';
			mxUtils.setOpacity(label, 10);
			var size = 12;
			
			var spinner = new Spinner({
				lines: 12, // The number of lines to draw
				length: size, // The length of each line
				width: 5, // The line thickness
				radius: 10, // The radius of the inner circle
				rotate: 0, // The rotation offset
				color: 'light-dark(#000000, #C0C0C0)', // #rgb or #rrggbb
				speed: 1.5, // Rounds per second
				trail: 60, // Afterglow percentage
				shadow: false, // Whether to render a shadow
				hwaccel: false, // Whether to use hardware acceleration
				top: '40%',
				zIndex: 2e9 // The z-index (defaults to 2000000000)
			});
			spinner.spin(button);
			
			// Timeout after 30 secs
			var timeout = window.setTimeout(function()
			{
				if (editorUi[clientName] == null)
				{
					spinner.stop();
					button.style.display = 'none';
				}
			}, 30000);
			
			editorUi.addListener('clientLoaded', mxUtils.bind(this, function()
			{
				if (editorUi[clientName] != null)
				{
					window.clearTimeout(timeout);
					mxUtils.setOpacity(label, 100);
					logo.style.visibility = '';
					spinner.stop();
					initButton();
				}
			}));
		}
		else
		{
			initButton();
		}

		buttons.appendChild(button);
		
		if (++count == rowLimit)
		{
			mxUtils.br(buttons);
			count = 0;
		}
	};

	if (!showButtons)
	{
		mxUtils.write(div, mxResources.get('chooseAnOption') + ':');
	}
	else
	{
		buttons.style.marginTop = '6px';
		div.appendChild(buttons);
	}
	
	// Adds all papersize options
	var serviceSelect = document.createElement('select');
	serviceSelect.style.marginLeft = '10px';

	if (!editorUi.isOfflineApp() && !editorUi.isOffline())
	{
		if (typeof window.DriveClient === 'function')
		{
			var googleOption = document.createElement('option');
			googleOption.setAttribute('value', App.MODE_GOOGLE);
			mxUtils.write(googleOption, mxResources.get('googleDrive'));
			serviceSelect.appendChild(googleOption);
			
			addLogo(IMAGE_PATH + '/google-drive-logo.svg', mxResources.get('googleDrive'), App.MODE_GOOGLE, 'drive');
		}
		
		if (typeof window.OneDriveClient === 'function')
		{
			var oneDriveOption = document.createElement('option');
			oneDriveOption.setAttribute('value', App.MODE_ONEDRIVE);
			mxUtils.write(oneDriveOption, mxResources.get('oneDrive'));
			serviceSelect.appendChild(oneDriveOption);
			
			if (editorUi.mode == App.MODE_ONEDRIVE)
			{
				oneDriveOption.setAttribute('selected', 'selected');
			}
			
			addLogo(IMAGE_PATH + '/onedrive-logo.svg', mxResources.get('oneDrive'), App.MODE_ONEDRIVE, 'oneDrive');
		}

		if (editorUi.m365 != null)
		{
			var m365Option = document.createElement('option');
			m365Option.setAttribute('value', App.MODE_M365);
			mxUtils.write(m365Option, mxResources.get('m365'));
			serviceSelect.appendChild(m365Option);

			if (editorUi.mode == App.MODE_M365)
			{
				m365Option.setAttribute('selected', 'selected');
			}

			addLogo(IMAGE_PATH + '/onedrive-logo.svg', mxResources.get('m365'), App.MODE_M365, 'm365');
		}

		if (typeof window.DropboxClient === 'function')
		{
			var dropboxOption = document.createElement('option');
			dropboxOption.setAttribute('value', App.MODE_DROPBOX);
			mxUtils.write(dropboxOption, mxResources.get('dropbox'));
			serviceSelect.appendChild(dropboxOption);
			
			if (editorUi.mode == App.MODE_DROPBOX)
			{
				dropboxOption.setAttribute('selected', 'selected');
			}
			
			addLogo(IMAGE_PATH + '/dropbox-logo.svg', mxResources.get('dropbox'), App.MODE_DROPBOX, 'dropbox');
		}

		if (editorUi.gitHub != null)
		{
			var gitHubOption = document.createElement('option');
			gitHubOption.setAttribute('value', App.MODE_GITHUB);
			mxUtils.write(gitHubOption, mxResources.get('github'));
			serviceSelect.appendChild(gitHubOption);
			
			addLogo(IMAGE_PATH + '/github-logo.svg', mxResources.get('github'), App.MODE_GITHUB, 'gitHub');
		}
		
		if (editorUi.gitLab != null)
		{
			var gitLabOption = document.createElement('option');
			gitLabOption.setAttribute('value', App.MODE_GITLAB);
			mxUtils.write(gitLabOption, mxResources.get('gitlab'));
			serviceSelect.appendChild(gitLabOption);

			addLogo(IMAGE_PATH + '/gitlab-logo.svg', mxResources.get('gitlab'), App.MODE_GITLAB, 'gitLab');
		}

		if (typeof window.TrelloClient === 'function')
		{
			var trelloOption = document.createElement('option');
			trelloOption.setAttribute('value', App.MODE_TRELLO);
			mxUtils.write(trelloOption, mxResources.get('trello'));
			serviceSelect.appendChild(trelloOption);
			
			addLogo(IMAGE_PATH + '/trello-logo.svg', mxResources.get('trello'), App.MODE_TRELLO, 'trello');
		}
	}
	
	if (!Editor.useLocalStorage || urlParams['storage'] == 'device' ||
		(editorUi.getCurrentFile() != null/* && !mxClient.IS_IOS*/ && urlParams['noDevice'] != '1'))
	{
		var deviceOption = document.createElement('option');
		deviceOption.setAttribute('value', App.MODE_DEVICE);
		mxUtils.write(deviceOption, mxResources.get('device'));
		serviceSelect.appendChild(deviceOption);
		
		if (editorUi.mode == App.MODE_DEVICE || !allowBrowser)
		{
			deviceOption.setAttribute('selected', 'selected');
		}
		
		if (showDeviceButton)
		{
			addLogo(IMAGE_PATH + '/osa_drive-harddisk.png', mxResources.get('device'), App.MODE_DEVICE);
		}
	}
	
	if (allowBrowser && isLocalStorage && urlParams['browser'] != '0')
	{
		var browserOption = document.createElement('option');
		browserOption.setAttribute('value', App.MODE_BROWSER);
		mxUtils.write(browserOption, mxResources.get('browser'));
		serviceSelect.appendChild(browserOption);
		
		if (editorUi.mode == App.MODE_BROWSER)
		{
			browserOption.setAttribute('selected', 'selected');
		}
		
		addLogo(IMAGE_PATH + '/osa_database.png', mxResources.get('browser'), App.MODE_BROWSER);
	}

	function change(newMode)
	{
		if (overrideExtension)
		{
			var fn = nameInput.value;
			var idx = fn.lastIndexOf('.');
			
			if (title.lastIndexOf('.') < 0 && (!showButtons || idx < 0))
			{
				newMode = (newMode != null) ? newMode : serviceSelect.value;
				var ext = '';
				
				if (newMode == App.MODE_GOOGLE)
				{
					ext = editorUi.drive.extension;
				}
				else if (newMode == App.MODE_GITHUB)
				{
					ext = editorUi.gitHub.extension;
				}
				else if (newMode == App.MODE_GITLAB)
				{
					ext = editorUi.gitLab.extension;
				}
				else if (newMode == App.MODE_TRELLO)
				{
					ext = editorUi.trello.extension;
				}
				else if (newMode == App.MODE_DROPBOX)
				{
					ext = editorUi.dropbox.extension;
				}
				else if (newMode == App.MODE_ONEDRIVE)
				{
					ext = editorUi.oneDrive.extension;
				}
				else if (newMode == App.MODE_DEVICE)
				{
					ext = '.drawio';
				}
				
				if (idx >= 0)
				{
					fn = fn.substring(0, idx);
				}
				
				nameInput.value = fn + ext;
			}
		}
	};

	var btns = document.createElement('div');
	btns.style.marginTop = (showButtons) ? '26px' : '38px';
	btns.style.textAlign = 'center';
	
	if (!showButtons)
	{
		div.appendChild(serviceSelect);
		mxEvent.addListener(serviceSelect, 'change', change);
		change();
	}
	
	if (helpLink != null)
	{
		var helpBtn = mxUtils.button(mxResources.get('help'), function()
		{
			editorUi.openLink(helpLink);
		});
		
		helpBtn.className = 'geBtn';
		btns.appendChild(helpBtn);
	}
	
	var cancelBtn = mxUtils.button(mxResources.get((cancelFn != null) ? 'close' : 'cancel'), function()
	{
		if (cancelFn != null)
		{
			cancelFn();
		}
		else
		{
			editorUi.fileLoaded(null);
			editorUi.hideDialog();
			window.close();
			window.location.href = editorUi.getUrl();
		}
	});
	
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst && cancelFn == null)
	{
		btns.appendChild(cancelBtn);
	}

	function create(mode)
	{
		var title = nameInput.value;
		
		if (mode == null || (title != null && title.length > 0))
		{
			if (hideDialog)
			{
				editorUi.hideDialog();
			}
			
			createFn(title, mode, nameInput);
		};
	}
	
	if (cancelFn == null)
	{
		var laterBtn = mxUtils.button(mxResources.get('decideLater'), function()
		{
			create(null);
		});
		
		laterBtn.className = 'geBtn';
		btns.appendChild(laterBtn);
	}

	if (allowTab && Editor.popupsAllowed)
	{
		var openBtn = mxUtils.button(mxResources.get('openInNewWindow'), function()
		{
			create('_blank');
		});
		
		openBtn.className = 'geBtn';
		btns.appendChild(openBtn);
	}

	if (CreateDialog.showDownloadButton)
	{
		var downloadButton = mxUtils.button(mxResources.get('download'), function()
		{
			create('download');
		});
		
		downloadButton.className = 'geBtn';
		btns.appendChild(downloadButton);
		
		if (copyBtn != null)
		{
			downloadButton.style.marginTop = '6px';
			btns.style.marginTop = '6px';
		}
	}
		
	if (copyBtn != null)
	{
		mxUtils.br(btns);
		btns.appendChild(copyBtn);
	}
	
	if (/*!mxClient.IS_IOS || */!showButtons)
	{
		var createBtn = mxUtils.button(btnLabel || mxResources.get('create'), function()
		{
			create((showDeviceButton) ? 'download' : ((showButtons) ? App.MODE_DEVICE : serviceSelect.value));
		});
		
		createBtn.className = 'geBtn gePrimaryBtn';
		btns.appendChild(createBtn);
	}
	
	if (!editorUi.editor.cancelFirst || cancelFn != null)
	{
		btns.appendChild(cancelBtn);
	}
	
	mxEvent.addListener(nameInput, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			create((showButtons) ? App.MODE_DEVICE : serviceSelect.value);
		}
		else if (e.keyCode == 27)
		{
			editorUi.fileLoaded(null);
			editorUi.hideDialog();
			window.close();
		}
	});

	div.appendChild(btns);
	this.container = div;
};

/**
 * 
 */
CreateDialog.showDownloadButton = urlParams['noDevice'] != '1';

/**
 * Constructs a new popup dialog.
 */
var PopupDialog = function(editorUi, url, pre, fallback, hideDialog) 
{
	hideDialog = (hideDialog != null) ? hideDialog : true;
	
	var div = document.createElement('div');
	div.style.textAlign = 'center';
	div.style.height = '100%';
	
	mxUtils.write(div, mxResources.get('fileOpenLocation'));
	mxUtils.br(div);
	mxUtils.br(div);

	var replaceBtn = mxUtils.button(mxResources.get('openInThisWindow'), function()
	{
		if (hideDialog)
		{
			editorUi.hideDialog();
		}
		
		if (fallback != null)
		{
			fallback();
		}
	});
	replaceBtn.className = 'geBtn';
	replaceBtn.style.marginBottom = '8px';
	replaceBtn.style.width = '280px';
	div.appendChild(replaceBtn);
	
	mxUtils.br(div);
	
	var wndBtn = mxUtils.button(mxResources.get('openInNewWindow'), function()
	{
		if (hideDialog)
		{
			editorUi.hideDialog();
		}

		if (pre != null)
		{
			pre();
		}
		
		editorUi.openLink(url, null, true);
	});
	wndBtn.className = 'geBtn gePrimaryBtn';
	wndBtn.style.width = replaceBtn.style.width;
	div.appendChild(wndBtn);
	
	mxUtils.br(div);
	mxUtils.br(div);
	mxUtils.write(div, mxResources.get('allowPopups'));
	
	this.container = div;
};

/**
 * Constructs a new image dialog.
 */
 var ImageDialog = function(editorUi, title, initialValue, fn, ignoreExisting, convertDataUri, withCrop, initClipPath)
{
	convertDataUri = (convertDataUri != null) ? convertDataUri : true;
	
	var graph = editorUi.editor.graph;
	var div = document.createElement('div');
	mxUtils.write(div, title);
	
	var inner = document.createElement('div');
	inner.className = 'geTitle';
	inner.style.backgroundColor = 'transparent';
	inner.style.borderColor = 'transparent';
	inner.style.whiteSpace = 'nowrap';
	inner.style.textOverflow = 'clip';
	inner.style.cursor = 'default';
	inner.style.paddingRight = '20px';
	
	var linkInput = document.createElement('input');
	linkInput.setAttribute('value', initialValue);
	linkInput.setAttribute('type', 'text');
	linkInput.setAttribute('spellcheck', 'false');
	linkInput.setAttribute('autocorrect', 'off');
	linkInput.setAttribute('autocomplete', 'off');
	linkInput.setAttribute('autocapitalize', 'off');
	linkInput.style.marginTop = '6px';
	var realWidth = (Graph.fileSupport) ? 460 : 340;
	linkInput.style.width = realWidth - 20 + 'px';
	linkInput.style.backgroundImage = 'url(\'' + Editor.crossImage + '\')';
	linkInput.style.backgroundRepeat = 'no-repeat';
	linkInput.style.backgroundPosition = '100% 50%';
	linkInput.style.paddingRight = '14px';
	
	var cross = document.createElement('div');
	cross.setAttribute('title', mxResources.get('reset'));
	cross.style.position = 'relative';
	cross.style.left = '-16px';
	cross.style.width = '12px';
	cross.style.height = '14px';
	cross.style.cursor = 'pointer';

	// Workaround for inline-block not supported in IE
	cross.style.display = 'inline-block';
	cross.style.top = '3px';
	
	// Needed to block event transparency in IE
	cross.style.background = 'url(\'' + editorUi.editor.transparentImage + '\')';

	mxEvent.addListener(cross, 'click', function()
	{
		linkInput.value = '';
		linkInput.focus();
	});
	
	inner.appendChild(linkInput);
	inner.appendChild(cross);
	div.appendChild(inner);

	var clipPath = initClipPath, cW, cH;

	var insertImage = function(newValue, w, h, resize)
	{
		var dataUri = newValue.substring(0, 5) == 'data:';
		
		if (!editorUi.isOffline() || (dataUri && typeof chrome === 'undefined'))
		{
			if (newValue.length > 0 && editorUi.spinner.spin(document.body, mxResources.get('inserting')))
			{
				var maxSize = 520;
				
				editorUi.loadImage(newValue, function(img)
				{
					editorUi.spinner.stop();
					editorUi.hideDialog();
					var s = (resize === false) ? 1 :
						(w != null && h != null) ? Math.max(w / img.width, h / img.height) :
						Math.min(1, Math.min(maxSize / img.width, maxSize / img.height));
					
					// Handles special case of data URI which needs to be rewritten
					// to be used in a cell style to remove the semicolon
					if (convertDataUri)
					{
						newValue = editorUi.convertDataUri(newValue);
					}
					
					fn(newValue, Math.round(Number(img.width) * s), Math.round(Number(img.height) * s), clipPath, cW, cH);
				}, function()
				{
					editorUi.spinner.stop();
					fn(null);
					editorUi.showError(mxResources.get('error'), mxResources.get('fileNotFound'), mxResources.get('ok'));
		    	});
			}
			else
			{
				editorUi.hideDialog();
				fn(newValue, null, null, clipPath, cW, cH);
			}
		}
		else
		{
			newValue = editorUi.convertDataUri(newValue);
			w = (w == null) ? 120 : w;
			h = (h == null) ? 100 : h;
			
			editorUi.hideDialog();				
			fn(newValue, w, h, clipPath, cW, cH);
		}
	};
	
	var apply = function(newValue, resize)
	{
		if (newValue != null)
		{
			var geo = (ignoreExisting) ? null : graph.getModel().getGeometry(graph.getSelectionCell());

			// Reuses width and height of existing cell
			if (geo != null)
			{
				insertImage(newValue, geo.width, geo.height, resize);
			}
			else
			{
				insertImage(newValue, null, null, resize);
			}
		}
		else
		{
			editorUi.hideDialog();
			fn(null);
		}
	};

	this.init = function()
	{
		linkInput.focus();
		
		// Installs drag and drop handler for local images and links
		if (Graph.fileSupport)
		{
			linkInput.setAttribute('placeholder', mxResources.get('dragImagesHere'));
			
			// Setup the dnd listeners
			var dlg = div.parentNode;
			var graph = editorUi.editor.graph;
			var dropElt = null;
				
			mxEvent.addListener(dlg, 'dragleave', function(evt)
			{
				if (dropElt != null)
			    {
			    	dropElt.parentNode.removeChild(dropElt);
			    	dropElt = null;
			    }
			    
				evt.stopPropagation();
				evt.preventDefault();
			});
			
			mxEvent.addListener(dlg, 'dragover', mxUtils.bind(this, function(evt)
			{
				if (dropElt == null)
				{
					dropElt = editorUi.highlightElement(dlg);
				}
				
				evt.stopPropagation();
				evt.preventDefault();
			}));
					
			mxEvent.addListener(dlg, 'drop', mxUtils.bind(this, function(evt)
			{
			    if (dropElt != null)
			    {
					dropElt.parentNode.removeChild(dropElt);
					dropElt = null;
			    }

			    if (evt.dataTransfer.files.length > 0)
			    {
			    	editorUi.importFiles(evt.dataTransfer.files, 0, 0, editorUi.maxImageSize, function(data, mimeType, x, y, w, h, fileName, resize)
			    	{
			    		apply(data, resize);
			    	}, function()
			    	{
			    		// No post processing
			    	}, function(file)
			    	{
			    		// Handles only images
			    		return file.type.substring(0, 6) == 'image/';
			    	}, function(queue)
			    	{
			    		// Invokes elements of queue in order
			    		for (var i = 0; i < queue.length; i++)
			    		{
			    			queue[i]();
			    		}
			    	}, !mxEvent.isControlDown(evt), null, null, true);
	    		}
			    else if (mxUtils.indexOf(evt.dataTransfer.types, 'text/uri-list') >= 0)
			    {
					var uri = evt.dataTransfer.getData('text/uri-list');
					
					if ((/\.(gif|jpg|jpeg|tiff|png|svg)($|\?)/i).test(uri))
					{
				    	apply(decodeURIComponent(uri));
					}
			    }

			    evt.stopPropagation();
			    evt.preventDefault();
			}), false);
		}
	};
	
	var btns = document.createElement('div');
	btns.style.marginTop = '14px';
	btns.style.textAlign = 'center';
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		// Just in case a spinner is spinning, has no effect otherwise
		editorUi.spinner.stop();
		editorUi.hideDialog();
	});
	
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	ImageDialog.filePicked = function(data)
	{
        if (data.action == google.picker.Action.PICKED)
        {
        	if (data.docs[0].thumbnails != null)
        	{
	        	var thumb = data.docs[0].thumbnails[data.docs[0].thumbnails.length - 1];
	        	
	        	if (thumb != null)
	        	{
	        		linkInput.value = thumb.url;
	        	}
        	}
        }
        
        linkInput.focus();
	};

	if (Graph.fileSupport)
	{
		if (editorUi.imgDlgFileInputElt == null)
		{
			var fileInput = document.createElement('input');
			fileInput.setAttribute('multiple', 'multiple');
			fileInput.setAttribute('type', 'file');
			
			mxEvent.addListener(fileInput, 'change', function(evt)
			{
				if (fileInput.files != null)
				{
					editorUi.importFiles(fileInput.files, 0, 0, editorUi.maxImageSize, function(data, mimeType, x, y, w, h)
			    	{
			    		apply(data);
			    	}, function()
			    	{
			    		// No post processing
			    	}, function(file)
			    	{
			    		// Handles only images
			    		return file.type.substring(0, 6) == 'image/';
			    	}, function(queue)
			    	{
			    		// Invokes elements of queue in order
			    		for (var i = 0; i < queue.length; i++)
			    		{
			    			queue[i]();
			    		}
			    	}, true);
					
		    		// Resets input to force change event for same file (type reset required for IE)
					fileInput.type = '';
					fileInput.type = 'file';
					fileInput.value = '';
				}
			});
			
			fileInput.style.display = 'none';
			document.body.appendChild(fileInput);
			editorUi.imgDlgFileInputElt = fileInput;
		}
		
		var btn = mxUtils.button(mxResources.get('open'), function()
		{
			editorUi.imgDlgFileInputElt.click();
		});

		btn.className = 'geBtn';
		btns.appendChild(btn);
	}

	mxEvent.addListener(linkInput, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			apply(linkInput.value);
		}
	});

	var cropBtn = mxUtils.button(mxResources.get('crop'), function()
	{
		var dlg = new CropImageDialog(editorUi, linkInput.value, clipPath, 
				function(clipPath_p, width, height)
			{
				clipPath = clipPath_p;
				cW = width;
				cH = height;
			});
	   
		editorUi.showDialog(dlg.container, 380, 390, true, true,
			null, null, null, new mxRectangle(0, 0, 440, 450));
	});
	
	if (withCrop)
 	{
 		cropBtn.className = 'geBtn';
		btns.appendChild(cropBtn);
	}
	
	var embedBtn = mxUtils.button(mxResources.get('embed'), function()
	{
		if (linkInput.value.substring(0, 5) != 'data:' && editorUi.spinner.spin(
			document.body, mxResources.get('loading')))
		{
			var converter = editorUi.editor.createImageUrlConverter();
			var src = converter.convert(linkInput.value);
			var img = new Image();

			img.onload = function()
			{
				editorUi.editor.convertImageToDataUri(src, function(uri)
				{
					editorUi.confirmImageResize(function(doResize)
					{
						editorUi.resizeImage(img, uri, mxUtils.bind(this, function(data2, w2, h2)
						{
							editorUi.spinner.stop();

							// Refuses to insert images above a certain size as they kill the app
							if (data2 != null && data2.length < editorUi.maxImageBytes)
							{
								linkInput.value = data2;
								updateButtonStates();
							}
							else
							{
								editorUi.handleError({message: mxResources.get('imageTooBig')});
							}
						}), doResize, editorUi.maxImageSize);
					}, img.width > editorUi.maxImageSize || img.height > editorUi.maxImageSize ||
						uri.length > editorUi.maxImageBytes);
				}, mxUtils.bind(this, function()
				{
					editorUi.handleError({message: mxResources.get('fileNotFound')});
				}));
			};

			img.onerror = function()
			{
				editorUi.spinner.stop();
				editorUi.handleError({message: mxResources.get('fileNotFound')});
			};

			img.src = src;
		}
	});

	function updateButtonStates()
	{
		if (linkInput.value.length > 0)
		{
			cropBtn.removeAttribute('disabled');
		}
		else
		{
			cropBtn.setAttribute('disabled', 'disabled');
		}

		if (linkInput.value.substring(0, 5) != 'data:')
		{
			embedBtn.removeAttribute('disabled');
		}
		else
		{
			embedBtn.setAttribute('disabled', 'disabled');
		}
	};

	embedBtn.className = 'geBtn';

	mxEvent.addListener(linkInput, 'change', function(e)
	{
		clipPath = null;
		updateButtonStates();
	});

	updateButtonStates();
	btns.appendChild(embedBtn);
	
	var applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		apply(linkInput.value);
	});
	
	applyBtn.className = 'geBtn gePrimaryBtn';
	btns.appendChild(applyBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}
	
	// Shows drop icon in dialog background
	if (Graph.fileSupport)
	{
		btns.style.marginTop = '120px';
		div.style.backgroundImage = 'url(\'' + IMAGE_PATH + '/droptarget.png\')';
		div.style.backgroundPosition = 'center 65%';
		div.style.backgroundRepeat = 'no-repeat';
		
		var bg = document.createElement('div');
		bg.style.position = 'absolute';
		bg.style.width = '420px';
		bg.style.top = '58%';
		bg.style.textAlign = 'center';
		bg.style.fontSize = '18px';
		bg.style.color = '#a0c3ff';
		mxUtils.write(bg, mxResources.get('dragImagesHere'));
		div.appendChild(bg);
	}

	div.appendChild(btns);

	this.container = div;
};

/**
 * Overrides link dialog to add Google Picker.
 */
var LinkDialog = function(editorUi, initialValue, btnLabel, fn, showPages, showNewWindowOption, linkTarget)
{
	var div = document.createElement('div');
	div.style.height = '100%';
	mxUtils.write(div, mxResources.get('editLink') + ':');
	
	var inner = document.createElement('div');
	inner.className = 'geUrlLinkContainer';
	inner.style.backgroundColor = 'transparent';
	inner.style.borderColor = 'transparent';
	inner.style.whiteSpace = 'nowrap';
	inner.style.textOverflow = 'clip';
	inner.style.cursor = 'default';

	var linkInput = document.createElement('input');
	linkInput.setAttribute('placeholder', mxResources.get('dragUrlsHere'));
	linkInput.setAttribute('type', 'text');
	linkInput.style.marginTop = '6px';
	linkInput.style.width = '414px';
	linkInput.style.boxSizing = 'border-box';
	linkInput.style.backgroundRepeat = 'no-repeat';
	linkInput.style.backgroundPosition = '100% 50%';
	linkInput.style.paddingRight = '16px';
	linkInput.style.marginBottom = '4px';

	var cross = document.createElement('div');
	cross.setAttribute('title', mxResources.get('reset'));
	cross.style.backgroundImage = 'url(' + Editor.crossImage + ')';
	cross.className = 'geAdaptiveAsset';
	cross.style.display = 'inline-block';
	cross.style.position = 'relative';
	cross.style.top = '3px';
	cross.style.left = '-16px';
	cross.style.width = '12px';
	cross.style.height = '14px';
	cross.style.cursor = 'pointer';
	
	mxEvent.addListener(cross, 'click', function()
	{
		linkInput.value = '';
		linkInput.focus();
	});

	var urlRadio = document.createElement('input');
	urlRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	urlRadio.setAttribute('value', 'url');
	urlRadio.setAttribute('type', 'radio');
	urlRadio.setAttribute('name', 'geLinkDialogOption');

	var pageRadio = document.createElement('input');
	pageRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	pageRadio.setAttribute('value', 'url');
	pageRadio.setAttribute('type', 'radio');
	pageRadio.setAttribute('name', 'geLinkDialogOption');

	var pageSelect = document.createElement('select');
	pageSelect.style.width = '414px';

	var newWindowCheckbox = document.createElement('input');
	newWindowCheckbox.setAttribute('type', 'checkbox');

	newWindowCheckbox.style.margin = '0 6p 0 6px';

	if (linkTarget != null)
	{
		newWindowCheckbox.setAttribute('checked', 'checked');
		newWindowCheckbox.defaultChecked = true;
	}
	
	linkTarget = (linkTarget != null) ? linkTarget : '_blank';
	newWindowCheckbox.setAttribute('title', linkTarget);
	
	if (showNewWindowOption)
	{
		linkInput.style.width = '200px';
	}
	
	if (showPages && editorUi.pages != null)
	{
		if (initialValue != null && Graph.isPageLink(initialValue))
		{
			pageRadio.setAttribute('checked', 'checked');
			pageRadio.defaultChecked = true;
		}
		else
		{
			linkInput.setAttribute('value', initialValue);
			urlRadio.setAttribute('checked', 'checked');
			urlRadio.defaultChecked = true;
		}
		
		inner.appendChild(urlRadio);
		inner.appendChild(linkInput);
		inner.appendChild(cross);
		
		if (showNewWindowOption)
		{
			inner.appendChild(newWindowCheckbox);
			mxUtils.write(inner, mxResources.get('openInNewWindow'));
		}
		
		mxUtils.br(inner);
		inner.appendChild(pageRadio);
		
		var pageFound = false;
		
		for (var i = 0; i < editorUi.pages.length; i++)
		{
			var pageOption = document.createElement('option');
			mxUtils.write(pageOption, editorUi.pages[i].getName() ||
				mxResources.get('pageWithNumber', [i + 1]));
			pageOption.setAttribute('value', 'data:page/id,' +
				editorUi.pages[i].getId());
			
			if (initialValue == pageOption.getAttribute('value'))
			{
				pageOption.setAttribute('selected', 'selected');
				pageFound = true;
			}

			pageSelect.appendChild(pageOption);
		}

		if (!pageFound && pageRadio.checked)
		{
			var notFoundOption = document.createElement('option');
			mxUtils.write(notFoundOption, mxResources.get('pageNotFound'));
			notFoundOption.setAttribute('disabled', 'disabled');
			notFoundOption.setAttribute('selected', 'selected');
			notFoundOption.setAttribute('value', 'pageNotFound');
			pageSelect.appendChild(notFoundOption);
			
			mxEvent.addListener(pageSelect, 'change', function()
			{
				if (notFoundOption.parentNode != null && !notFoundOption.selected)
				{
					notFoundOption.parentNode.removeChild(notFoundOption);
				}
			});
		}
		
		inner.appendChild(pageSelect);
	}
	else
	{
		linkInput.setAttribute('value', initialValue);
		inner.appendChild(linkInput);
		inner.appendChild(cross);
	}

	div.appendChild(inner);
	
	var mainBtn = mxUtils.button(btnLabel, function()
	{
		editorUi.hideDialog();
		var value = (pageRadio.checked) ? ((pageSelect.value !== 'pageNotFound') ?
			pageSelect.value : initialValue) : linkInput.value;
		fn(value, LinkDialog.selectedDocs, (newWindowCheckbox.checked) ? linkTarget : null);
	});
	mainBtn.style.verticalAlign = 'middle';
	mainBtn.className = 'geBtn gePrimaryBtn';
	
	this.init = function()
	{
		if (pageRadio.checked)
		{
			pageSelect.focus();
		}
		else
		{
			linkInput.focus();
			
			if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
			{
				linkInput.select();
			}
			else
			{
				document.execCommand('selectAll', false, null);
			}
		}
		
		mxEvent.addListener(pageSelect, 'focus', function()
		{
			urlRadio.removeAttribute('checked');
			pageRadio.setAttribute('checked', 'checked');
			pageRadio.checked = true;
		});
		
		mxEvent.addListener(linkInput, 'focus', function()
		{
			pageRadio.removeAttribute('checked');
			urlRadio.setAttribute('checked', 'checked');
			urlRadio.checked = true;
		});

		// Installs drag and drop handler for links
		if (Graph.fileSupport)
		{
			// Setup the dnd listeners
			var dlg = div.parentNode;
			var dropElt = null;
				
			mxEvent.addListener(dlg, 'dragleave', function(evt)
			{
				if (dropElt != null)
			    {
			    	dropElt.parentNode.removeChild(dropElt);
			    	dropElt = null;
			    }
			    
				evt.stopPropagation();
				evt.preventDefault();
			});
			
			mxEvent.addListener(dlg, 'dragover', mxUtils.bind(this, function(evt)
			{
				if (dropElt == null)
				{
					dropElt = editorUi.highlightElement(dlg);
				}
				
				evt.stopPropagation();
				evt.preventDefault();
			}));
					
			mxEvent.addListener(dlg, 'drop', mxUtils.bind(this, function(evt)
			{
				try
				{
					if (dropElt != null)
					{
						dropElt.parentNode.removeChild(dropElt);
						dropElt = null;
					}

					if (mxUtils.indexOf(evt.dataTransfer.types, 'text/uri-list') >= 0)
					{
						linkInput.value = decodeURIComponent(evt.dataTransfer.getData('text/uri-list'));
						urlRadio.setAttribute('checked', 'checked');
						urlRadio.checked = true;
						mainBtn.click();
					}
				}
				catch (e)
				{
					editorUi.handleError(e);
				}

			    evt.stopPropagation();
			    evt.preventDefault();
			}), false);
		}
	};
	
	var btns = document.createElement('div');
	btns.style.marginTop = '16px';
	btns.style.textAlign = 'right';

	if (!editorUi.isOffline())
	{
		btns.appendChild(editorUi.createHelpIcon('https://www.drawio.com/doc/faq/custom-links'));
	}
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	cancelBtn.style.verticalAlign = 'middle';
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}
	
	LinkDialog.selectedDocs = null;
	
	LinkDialog.filePicked = function(data)
	{
		if (data.action == google.picker.Action.PICKED)
        {
			LinkDialog.selectedDocs = data.docs;
        	var href = data.docs[0].url;
        	
    		if (data.docs[0].mimeType == 'application/mxe' || (data.docs[0].mimeType != null &&
    			data.docs[0].mimeType.substring(0, 23) == 'application/vnd.jgraph.'))
    		{
				href = 'https://app.diagrams.net/#G' + data.docs[0].id;
    		}
    		else if (data.docs[0].mimeType == 'application/vnd.google-apps.folder')
    		{
    			// Do not use folderview in data.docs[0].url link to Google Drive instead
    			href = 'https://drive.google.com/#folders/' + data.docs[0].id;
    		}
    		
    		linkInput.value = href;
    		linkInput.focus();
        }
		else
		{
			LinkDialog.selectedDocs = null;
		}
		
		linkInput.focus();
	};

	var selectDropdown = document.createElement('select');
	selectDropdown.className = 'geBtn';
	selectDropdown.style.position = 'relative';
	selectDropdown.style.top = '1px';
	selectDropdown.style.maxWidth = '120px';
	var selectFn = {};

	var option = document.createElement('option');
	mxUtils.write(option, mxResources.get('select') + '...');
	option.value = '';
	selectDropdown.appendChild(option);

	function addButton(key, fn)
	{
		var option = document.createElement('option');
		mxUtils.write(option, mxResources.get(key));
		option.value = key;
		selectDropdown.appendChild(option);
		selectFn[key] = fn;
	};

	if (typeof(google) != 'undefined' && typeof(google.picker) != 'undefined' && editorUi.drive != null)
	{
		addButton('googleDrive', function()
		{
			if (editorUi.spinner.spin(document.body, mxResources.get('authorizing')))
			{
				editorUi.drive.checkToken(mxUtils.bind(this, function()
				{
					editorUi.spinner.stop();
					
					// Creates one picker and reuses it to avoid polluting the DOM
					if (editorUi.linkPicker == null)
					{
						var picker = editorUi.drive.createLinkPicker();

						editorUi.linkPicker = picker.setCallback(function(data)
						{
							LinkDialog.filePicked(data);
					    }).build();
					}
					
					editorUi.linkPicker.setVisible(true);
				}));
			}
		});
	}
	
	if (typeof(Dropbox) != 'undefined' && typeof(Dropbox.choose) != 'undefined')
	{
		addButton('dropbox', function()
		{
			// Authentication will be carried out on open to make sure the
			// autosave does not show an auth dialog. Showing it here will
			// block the second dialog (the file picker) so it's too early.
			Dropbox.choose(
			{
				linkType : 'direct',
				cancel: function()
				{
					// do nothing
		        },
				success : function(files)
				{
					linkInput.value = files[0].link;
					linkInput.focus();
				}
			});
		});
	}
	
	if (editorUi.oneDrive != null)
	{
		addButton('oneDrive', function()
		{
			editorUi.oneDrive.pickFile(function(id, files)
			{
				if (files != null && files.value != null && files.value.length > 0)
				{
					linkInput.value = files.value[0].webUrl;
					linkInput.focus();
				}
			}, true);
		});
	}
	
	if (editorUi.gitHub != null)
	{
		addButton('github', function()
		{
			editorUi.gitHub.pickFile(function(path)
			{
				if (path != null)
				{
					var tokens = path.split('/');
					var org = tokens[0];
					var repo = tokens[1];
					var ref = tokens[2];
					var path = tokens.slice(3, tokens.length).join('/');
					
					linkInput.value = 'https://github.com/' + org + '/' +
						repo + '/blob/' + ref + '/' + path;
					linkInput.focus();
				}
			});
		});
	}
	
	if (editorUi.gitLab != null)
	{
		addButton('gitlab', function()
		{
			editorUi.gitLab.pickFile(function(path)
			{
				if (path != null)
				{
					var tokens = path.split('/');
					var org = tokens[0];
					var repo = tokens[1];
					var ref = tokens[2];
					var path = tokens.slice(3, tokens.length).join('/');

					linkInput.value = DRAWIO_GITLAB_URL + '/' + org + '/' +
						repo + '/blob/' + ref + '/' + path;
					linkInput.focus();
				}
			});
		});
	}

	if (selectDropdown.children.length > 1)
	{
		btns.appendChild(selectDropdown);

		mxEvent.addListener(selectDropdown, 'change', function(evt)
		{
			if (selectFn[selectDropdown.value] != null)
			{
				selectFn[selectDropdown.value]();
				selectDropdown.value = '';
			}
		});
	}

	mxEvent.addListener(linkInput, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			mainBtn.click();
		}
	});

	btns.appendChild(mainBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	div.appendChild(btns);

	this.container = div;
};


/**
 * Constructs a new revision dialog
 */
var RevisionDialog = function(editorUi, revs, restoreFn)
{
	var div = document.createElement('div');
	
	var title = document.createElement('h3');
	title.style.marginTop = '3px';
	mxUtils.write(title, mxResources.get('revisionHistory'));
	div.appendChild(title);
	
	var list = document.createElement('div');
	list.style.position = 'absolute';
	list.style.overflow = 'auto';
	list.style.width = '170px';
	list.style.height = '378px';
	div.appendChild(list);
	
	var container = document.createElement('div');
	container.style.position = 'absolute';
	container.style.left = '200px';
	container.style.width = '470px';
	container.style.height = '376px';
	container.style.overflow = 'hidden';
	container.style.borderWidth = '1px';
	container.style.borderStyle = 'solid';

	// Contains possible error messages
	var errorNode = document.createElement('div');
	errorNode.style.position = 'absolute',
	errorNode.style.display = 'none';
	errorNode.style.textAlign = 'center';
	errorNode.style.padding = '8px';
	errorNode.style.borderRadius = '8px';
	errorNode.style.left = '50%';
	errorNode.style.top = '50%';
	errorNode.style.whiteSpace = 'nowrap';
	errorNode.style.transform = 'translate(-50%, -50%)';
	errorNode.style.background = 'inherit';
	errorNode.style.border = '1px solid';
	container.appendChild(errorNode);
	
	mxEvent.disableContextMenu(container);
	div.appendChild(container);

	var graph = new Graph(container);
	graph.setTooltips(false);
	graph.setEnabled(false);
	graph.setPanning(true);
	graph.panningHandler.ignoreCell = true;
	graph.panningHandler.useLeftButtonForPanning = true;
	graph.minFitScale = null;
	graph.maxFitScale = null;
	graph.centerZoom = true;
	
	// Handles placeholders for pages
	var currentPage = 0;
	var diagrams = null;
	var realPage = 0;
	
	var graphGetGlobalVariable = graph.getGlobalVariable;
	
	graph.getGlobalVariable = function(name)
	{
		if (name == 'page' && diagrams != null && diagrams[realPage] != null)
		{
			return diagrams[realPage].getAttribute('name');
		}
		else if (name == 'pagenumber')
		{
			return realPage + 1;
		}
		else if (name == 'pagecount')
		{
			return (diagrams != null) ? diagrams.length : 1;
		}
		
		return graphGetGlobalVariable.apply(this, arguments);
	};
	
	// Disables hyperlinks
	graph.getLinkForCell = function()
	{
		return null;
	};

	var opts = {
	  lines: 11, // The number of lines to draw
	  length: 15, // The length of each line
	  width: 6, // The line thickness
	  radius: 10, // The radius of the inner circle
	  corners: 1, // Corner roundness (0..1)
	  rotate: 0, // The rotation offset
	  direction: 1, // 1: clockwise, -1: counterclockwise
	  color: 'light-dark(#000000, #C0C0C0)', // #rgb or #rrggbb
	  speed: 1.4, // Rounds per second
	  trail: 60, // Afterglow percentage
	  shadow: false, // Whether to render a shadow
	  hwaccel: false, // Whether to use hardware acceleration
	  className: 'spinner', // The CSS class to assign to the spinner
	  zIndex: 2e9, // The z-index (defaults to 2000000000)
	  top: '50%', // Top position relative to parent
	  left: '50%' // Left position relative to parent
	};
	
	var spinner = new Spinner(opts);

	var file = editorUi.getCurrentFile();
	var fileNode = editorUi.getXmlFileData(true, false, true);
	var tmp = fileNode.getElementsByTagName('diagram');
	var currentDiagrams = {};
	
	for (var i = 0; i < tmp.length; i++)
	{
		currentDiagrams[tmp[i].getAttribute('id')] = tmp[i];
	}

	var currentRow = null;
	var currentRev = null;
	var currentDoc = null;
	var currentXml = null;

	var zoomInBtn = editorUi.createToolbarButton(Editor.zoomInImage, mxResources.get('zoomIn'), function()
	{
		if (currentDoc != null)
		{
			graph.zoomIn();
		}
	}, 20);

	zoomInBtn.setAttribute('disabled', 'disabled');

	var zoomOutBtn = editorUi.createToolbarButton(Editor.zoomOutImage, mxResources.get('zoomOut'), function()
	{
		if (currentDoc != null)
		{
			graph.zoomOut();
		}
	}, 20);

	zoomOutBtn.setAttribute('disabled', 'disabled');

	var zoomFitBtn = editorUi.createToolbarButton(Editor.zoomFitImage, mxResources.get('fit'), function()
	{
		if (currentDoc != null)
		{
			if (graph.view.scale == 1)
			{
				graph.maxFitScale = 8;
				graph.fit(8);
			}
			else
			{
				graph.zoomActual();
			}

			graph.center();
		}
	}, 20);

	zoomFitBtn.setAttribute('disabled', 'disabled');

	// Gesture listener added below to handle pressed state
	var compareBtn = editorUi.createToolbarButton(Editor.compareImage, mxResources.get('compare'));
	compareBtn.setAttribute('disabled', 'disabled');

	var mergeBtn = editorUi.createToolbarButton(Editor.thinDataImage, mxResources.get('merge'));
	mergeBtn.setAttribute('disabled', 'disabled');
	
	var cmpContainer = container.cloneNode(false);
	cmpContainer.style.pointerEvent = 'none';
	container.parentNode.appendChild(cmpContainer);

	var cmpGraph = new Graph(cmpContainer);
	cmpGraph.setTooltips(false);
	cmpGraph.setEnabled(false);
	cmpGraph.setPanning(true);
	cmpGraph.panningHandler.ignoreCell = true;
	cmpGraph.panningHandler.useLeftButtonForPanning = true;
	cmpGraph.minFitScale = null;
	cmpGraph.maxFitScale = null;
	cmpGraph.centerZoom = true;

	var fileInfo = document.createElement('div');
	fileInfo.style.textAlign = 'left';
	fileInfo.style.color = 'gray';
	fileInfo.style.backgroundColor = 'transparent';
	fileInfo.style.overflow = 'hidden';
	fileInfo.style.textOverflow = 'ellipsis';
	fileInfo.style.whiteSpace = 'nowrap';
	fileInfo.style.cursor = 'default';
	fileInfo.style.height = '100%';
	fileInfo.style.display = 'inline-flex';
	fileInfo.style.alignItems = 'center';
	fileInfo.style.flexGrow = '1';

	mxEvent.addListener(fileInfo, 'click', function()
	{
		var textContent = mxUtils.getTextContent(fileInfo);

		if (textContent != '')
		{
			editorUi.alert(textContent);
		}
	});

	var prevFileInfo = null;

	mxEvent.addGestureListeners(compareBtn, function(e)
	{
		// Gets current state of page with given ID
		try
		{
			var curr = (diagrams[currentPage] != null) ? currentDiagrams[
				diagrams[currentPage].getAttribute('id')] : null;
			mxUtils.setOpacity(compareBtn, 20);
			errorNode.innerText = '';

			if (curr == null)
			{
				errorNode.style.display = 'inline-block';
				mxUtils.write(errorNode, mxResources.get('pageNotFound'));
			}
			else
			{
				prevFileInfo = fileInfo.innerHTML;
				fileInfo.innerHTML = mxResources.get('current');
				container.style.display = 'none';
				cmpContainer.style.display = '';
				cmpContainer.style.backgroundColor = container.style.backgroundColor;

				var tempNode = Editor.parseDiagramNode(curr);
				var codec = new mxCodec(tempNode.ownerDocument);
				codec.decode(tempNode, cmpGraph.getModel());
				cmpGraph.view.scaleAndTranslate(graph.view.scale,
					graph.view.translate.x, graph.view.translate.y);
				cmpGraph.mathEnabled = tempNode.getAttribute('math') == '1';
				cmpGraph.setAdaptiveColors(tempNode.getAttribute('adaptiveColors'));
				
				if (Editor.MathJaxRender && cmpGraph.mathEnabled)
				{
					Editor.MathJaxRender(cmpGraph.container);
				}
			}
		}
		catch (e)
		{
			errorNode.style.display = 'inline-block';
			errorNode.innerText = '';
			mxUtils.write(errorNode, mxResources.get('pageNotFound') + ': ' + e.message);
		}
	}, null, function()
	{
		mxUtils.setOpacity(compareBtn, 60);
		errorNode.style.display = 'none';
		errorNode.innerText = '';

		if (container.style.display == 'none')
		{
			container.style.display = '';
			fileInfo.innerHTML = prevFileInfo;
			cmpContainer.style.display = 'none';
		}
	});

	mxEvent.addListener(mergeBtn, 'click', mxUtils.bind(this, function(e)
	{
		if (currentDoc != null)
		{
			var pages = editorUi.getPagesForNode(currentDoc.documentElement);
			var patch = editorUi.diffPages(editorUi.pages, pages);

			var dlg = new TextareaDialog(editorUi, mxResources.get('merge') + ':',
				JSON.stringify(patch, null, 2), function(newValue)
			{
				try
				{
					if (newValue.length > 0 && editorUi.editor.graph.isEnabled())
					{
						var patches = [JSON.parse(newValue)];

						editorUi.confirm(mxResources.get('areYouSure'), function()
						{
							try
							{
								file.patch(patches, null, true, true);

								// Hides compare dialog
								editorUi.hideDialog();

								// Hides revision history dialog
								editorUi.hideDialog();
							}
							catch (e)
							{
								editorUi.handleError(e);
							}
						});
					}
					else
					{
						// Hides compare dialog
						editorUi.hideDialog();
					}
				}
				catch (e)
				{
					editorUi.handleError(e);
				}
			}, null, null, null, null, function(buttons, input)
			{
				// Adds a checkbox to only use the current page diff
				if (patch[EditorUi.DIFF_UPDATE] != null)
				{
					var patchSelect = document.createElement('select');
					var option = document.createElement('option');
					mxUtils.write(option, mxResources.get('allPages'));
					option.setAttribute('value', 'allPages');
					patchSelect.appendChild(option);

					for (var pageId in patch[EditorUi.DIFF_UPDATE])
					{
						var option = document.createElement('option');
						var page = editorUi.getPageById(pageId);

						if (page != null)
						{
							mxUtils.write(option, page.getName());
							option.setAttribute('value', 'page-' + pageId);
							patchSelect.appendChild(option);
						}
					}

					patchSelect.style.marginRight = '8px';
					patchSelect.style.order = '-1';

					mxEvent.addListener(patchSelect, 'change', function(evt)
					{
						var pagePatch = null;

						if (patchSelect.value != 'allPages')
						{
							var pageId = patchSelect.value.substring(5);

							if (patch[EditorUi.DIFF_UPDATE][pageId] != null)
							{
								pagePatch = new Object();
								pagePatch[EditorUi.DIFF_UPDATE] = new Object();
								pagePatch[EditorUi.DIFF_UPDATE][pageId] =
									patch[EditorUi.DIFF_UPDATE][pageId];
							}
						}

						input.value = JSON.stringify((pagePatch == null) ?
							patch : pagePatch, null, 2);
					});

					if (patchSelect.children.length > 2)
					{
						buttons.appendChild(patchSelect);
					}
				}
			}, true, null, mxResources.get(editorUi.editor.graph.isEnabled() ?
				'merge' : 'close'));
			
			editorUi.showDialog(dlg.container, 620, 460, true, true);
			dlg.init();
		}
	}));

	var restoreBtn = mxUtils.button(mxResources.get('restore'), function(e)
	{
		if (currentDoc != null && currentXml != null)
		{
			editorUi.confirm(mxResources.get('areYouSure'), function()
			{
				if (restoreFn != null)
				{
					restoreFn(currentXml);
				}
				else
				{
					if (editorUi.spinner.spin(document.body, mxResources.get('restoring')))
					{
						restoreBtn.setAttribute('disabled', 'disabled');
				
						file.save(true, function(resp)
						{
							editorUi.spinner.stop();
							restoreBtn.removeAttribute('disabled');
							editorUi.replaceFileData(currentXml);
							editorUi.hideDialog();
						}, function(resp)
						{
							editorUi.spinner.stop();
							restoreBtn.removeAttribute('disabled');
							editorUi.clearStatus();
							editorUi.handleError(resp, (resp != null) ? mxResources.get('errorSavingFile') : null);
						});
					}
				}
			});
		}
	});

	restoreBtn.className = 'geBtn gePrimaryBtn';
	restoreBtn.setAttribute('disabled', 'disabled');
	
	var pageSelect = document.createElement('select');
	pageSelect.setAttribute('disabled', 'disabled');
	pageSelect.style.userSelect = 'none';
	pageSelect.style.maxWidth = '100px';
	pageSelect.style.marginLeft = '10px';
	pageSelect.style.display = 'none';
	
	var pageSelectFunction = null;
	
	mxEvent.addListener(pageSelect, 'change', function(evt)
	{
		if (pageSelectFunction != null)
		{
			pageSelectFunction(evt);
			mxEvent.consume(evt);
		}
	});
	
	var newBtn = mxUtils.button(mxResources.get('open'), function()
	{
		if (currentDoc != null)
		{
			window.openFile = new OpenFile(function()
			{
				window.openFile = null;
			});
			
			window.openFile.setData(mxUtils.getXml(currentDoc.documentElement));
			editorUi.openLink(editorUi.getUrl(), null, true);
		}
	});

	newBtn.className = 'geBtn';
	newBtn.setAttribute('disabled', 'disabled');

	var createBtn = mxUtils.button(mxResources.get('createRevision'), function()
	{
		editorUi.actions.get('save').funct(false);
	});

	createBtn.className = 'geBtn';
	createBtn.setAttribute('disabled', 'disabled');
	
	if (restoreFn != null)
	{
		newBtn.style.display = 'none';
	}
	
	var buttons = document.createElement('div');
	buttons.style.position = 'absolute';
	buttons.style.top = '482px';
	buttons.style.right = '28px';
	buttons.style.left = '32px';
	buttons.style.justifyContent = 'end';
	buttons.style.display = 'flex';

	var tb = document.createElement('div');
	tb.className = 'geToolbarContainer';
	tb.style.backgroundColor = 'transparent';
	tb.style.left = '32px';
	tb.style.right = '32px';
	tb.style.border = 'none';
	tb.style.top = '442px';

	var currentElt = null;
	
	if (revs != null && revs.length > 0)
	{
		container.style.cursor = 'move';
		
		var table = document.createElement('table');
		table.style.borderWidth = '1px';
		table.style.borderStyle = 'solid';
		table.style.borderCollapse = 'collapse';
		table.style.borderSpacing = '0px';
		table.style.width = '100%';
		var tbody = document.createElement('tbody');
		var today = new Date().toDateString();

		if (editorUi.currentPage != null && editorUi.pages != null)
		{
			currentPage = mxUtils.indexOf(editorUi.pages, editorUi.currentPage);
		}
		
		for (var i = revs.length - 1; i >= 0; i--)
		{
			var elt = (function(item)
			{
				var ts = new Date(item.modifiedDate);
				var row = null;
				var pd = '6px';
				
				// Workaround for negative timestamps in Dropbox
				if (ts.getTime() >= 0)
				{
					row = document.createElement('tr');
					row.style.borderBottomWidth = '1px';
					row.style.borderBottomStyle = 'solid';
					row.style.fontSize = '12px';
					row.style.cursor = 'pointer';
					
					var date = document.createElement('td');
					date.style.padding = pd;
					date.style.whiteSpace = 'nowrap';
					
					if (item == revs[revs.length - 1])
					{
						mxUtils.write(date, mxResources.get('current'));
					}
					else
					{
						if (ts.toDateString() === today)
						{
							mxUtils.write(date, ts.toLocaleTimeString());
						}
						else
						{
							mxUtils.write(date, ts.toLocaleDateString() + ' ' +
								ts.toLocaleTimeString());
						}
					}
					
					row.appendChild(date);

					row.setAttribute('title', ts.toLocaleDateString() + ' ' +
						ts.toLocaleTimeString() + ((item.fileSize != null)? ' ' +
						editorUi.formatFileSize(parseInt(item.fileSize)) : '') +
						((item.lastModifyingUserName != null) ? ' ' +
						item.lastModifyingUserName : ''));

					function updateGraph(xml)
					{
						spinner.stop();
						errorNode.innerText = '';
						var doc = mxUtils.parseXml(xml);
						var node = editorUi.editor.extractGraphModel(doc.documentElement, true);

						if (node != null)
						{
							pageSelect.style.display = 'none';
							pageSelect.innerText = '';
							currentDoc = doc;
							currentXml = xml;
							parseSelectFunction = null;
							diagrams = null;
							realPage = 0;
							
							function parseGraphModel(dataNode)
							{
								var bg = dataNode.getAttribute('background');
								
								if (bg == null || bg == '' || bg == mxConstants.NONE)
								{
									bg = graph.defaultPageBackgroundColor;
								}
								
								container.style.backgroundColor = mxUtils.getLightDarkColor(bg);
								
								var codec = new mxCodec(dataNode.ownerDocument);
								codec.decode(dataNode, graph.getModel());
								graph.maxFitScale = 1;
								graph.fit(8);
								graph.center();
								graph.mathEnabled = dataNode.getAttribute('math') == '1';
								graph.setAdaptiveColors(dataNode.getAttribute('adaptiveColors'));

								if (Editor.MathJaxRender && graph.mathEnabled)
								{
									Editor.MathJaxRender(graph.container);
								}

								return dataNode;
							};
							
							function parseDiagram(diagramNode)
							{
								if (diagramNode != null)
								{
									diagramNode = parseGraphModel(Editor.parseDiagramNode(diagramNode));
								}
								
								return diagramNode;
							};

							if (node.nodeName == 'mxfile')
							{
								// Workaround for "invalid calling object" error in IE
								var tmp = node.getElementsByTagName('diagram');
								var newPages = {};
								diagrams = [];
								
								for (var i = 0; i < tmp.length; i++)
								{
									diagrams.push(tmp[i]);
								}
								
								realPage = Math.min(currentPage, diagrams.length - 1);
								
								if (diagrams.length > 0)
								{
									parseDiagram(diagrams[realPage]);
								}
								
								if (diagrams.length > 1)
								{
									pageSelect.removeAttribute('disabled');
									pageSelect.style.display = '';

									for (var i = 0; i < diagrams.length; i++)
									{
										var pageOption = document.createElement('option');
										pageOption.setAttribute('title', name + ' (' +
											diagrams[i].getAttribute('id') + ')');
										pageOption.setAttribute('value', i);
										var name = diagrams[i].getAttribute('name') ||
											mxResources.get('pageWithNumber', [i + 1]);
										var localPage = editorUi.getPageById(diagrams[i].getAttribute('id'));
										var state = '';
										
										if (localPage != null)
										{
											var newPage = new DiagramPage(diagrams[i]);

											if (editorUi.getHashValueForPages([localPage]) != editorUi.getHashValueForPages([newPage]))
											{
												state = ' (M)';
											}
										}
										else
										{
											state = ' (X)';
										}

										mxUtils.write(pageOption, name + state);
										
										if (i == realPage)
										{
											pageOption.setAttribute('selected', 'selected');
										}
										
										pageSelect.appendChild(pageOption);
									}
								}
								
								pageSelectFunction = function()
								{
									try
									{
										var temp = parseInt(pageSelect.value);
										currentPage = temp;
										realPage = currentPage;
										parseDiagram(diagrams[temp]);
									}
									catch (e)
									{
										pageSelect.value = currentPage;
										editorUi.handleError(e);
									}
								};
							}
							else
							{
								parseGraphModel(node);
							}
							
							var shortUser = item.lastModifyingUserName;
							
							if (shortUser != null && shortUser.length > 20)
							{
								shortUser = shortUser.substring(0, 20) + '...';
							}
							
							fileInfo.innerText = '';
							mxUtils.write(fileInfo, ((shortUser != null) ?
								(shortUser + ' ') : '') + ts.toLocaleDateString() +
								' ' + ts.toLocaleTimeString());
							
							fileInfo.setAttribute('title', row.getAttribute('title'));
							zoomInBtn.removeAttribute('disabled');
							zoomOutBtn.removeAttribute('disabled');
							zoomFitBtn.removeAttribute('disabled');
							compareBtn.removeAttribute('disabled');
							mergeBtn.removeAttribute('disabled');
							
							if (file == null || !file.isRestricted())
							{
								if (editorUi.editor.graph.isEnabled())
								{
									restoreBtn.removeAttribute('disabled');
								}

								newBtn.removeAttribute('disabled');
								createBtn.removeAttribute('disabled');
							}
							
							mxUtils.setOpacity(zoomInBtn, 60);
							mxUtils.setOpacity(zoomOutBtn, 60);
							mxUtils.setOpacity(zoomFitBtn, 60);
							mxUtils.setOpacity(compareBtn, 60);
							mxUtils.setOpacity(mergeBtn, 60);
						}
						else
						{
							pageSelect.style.display = 'none';
							pageSelect.innerText = '';
							fileInfo.innerText = '';
							errorNode.innerText = '';
							mxUtils.write(fileInfo, mxResources.get('errorLoadingFile'));
							mxUtils.write(errorNode, mxResources.get('errorLoadingFile'));
						}
					};
					
					mxEvent.addListener(row, 'click', function(evt)
					{
						if (currentRev != item)
						{
							spinner.stop();
							
							if (currentRow != null)
							{
								currentRow.style.backgroundColor = '';
							}
							
							currentRev = item;
							currentRow = row;
							currentRow.style.backgroundColor = 'light-dark(#ebf2f9, #000000)';
							currentDoc = null;
							currentXml = null;

							fileInfo.removeAttribute('title');
							fileInfo.innerText = mxResources.get('loading') + '...';
							container.style.backgroundColor = graph.defaultPageBackgroundColor;
							errorNode.innerText = '';
							graph.getModel().clear();
							
							restoreBtn.setAttribute('disabled', 'disabled');
							zoomInBtn.setAttribute('disabled', 'disabled');
							zoomOutBtn.setAttribute('disabled', 'disabled');
							zoomFitBtn.setAttribute('disabled', 'disabled');
							compareBtn.setAttribute('disabled', 'disabled');
							mergeBtn.setAttribute('disabled', 'disabled');

							newBtn.setAttribute('disabled', 'disabled');
							pageSelect.setAttribute('disabled', 'disabled');
							
							mxUtils.setOpacity(zoomInBtn, 20);
							mxUtils.setOpacity(zoomOutBtn, 20);
							mxUtils.setOpacity(zoomFitBtn, 20);
							mxUtils.setOpacity(compareBtn, 20);
							mxUtils.setOpacity(mergeBtn, 20);

							spinner.spin(container);
							
							item.getXml(function(xml)
				   			{
								if (currentRev == item)
								{
									try
									{
										updateGraph(xml);
									}
									catch (e)
									{
										fileInfo.innerText = mxResources.get('error') + ': ' + e.message;
									}
								}
				   			}, function(err)
				   			{
				   				spinner.stop();
								pageSelect.style.display = 'none';
								pageSelect.innerText = '';
				   				fileInfo.innerText = '';
								mxUtils.write(fileInfo, mxResources.get('errorLoadingFile'));
								mxUtils.write(errorNode, mxResources.get('errorLoadingFile'));
				   			});

							mxEvent.consume(evt);
						}
					});
					
					mxEvent.addListener(row, 'dblclick', function(evt)
					{
						newBtn.click();
						
						if (window.getSelection)
						{
							window.getSelection().removeAllRanges();
						}
					    else if (document.selection)
					    {
					    	document.selection.empty();
					    }
						
						mxEvent.consume(evt);
					}, false);
					
					tbody.appendChild(row);
				}

				return row;
			})(revs[i]);
			
			// Selects and loads first element in list (ie current version) after
			// graph container was initialized since there is no loading delay
			if (elt != null && i == revs.length - 1)
			{
				currentElt = elt;
			}
		}
		
		table.appendChild(tbody);
		list.appendChild(table);
	}
	else if (file == null || (editorUi.drive == null && file.constructor == window.DriveFile) ||
		(editorUi.dropbox == null && file.constructor == window.DropboxFile))
	{
		container.style.display = 'none';
		tb.style.display = 'none';
		mxUtils.write(list, mxResources.get('notAvailable'));
	}
	else
	{
		container.style.display = 'none';
		tb.style.display = 'none';
		mxUtils.write(list, mxResources.get('noRevisions'));
	}
	
	this.init = function()
	{
		if (currentElt != null)
		{
			currentElt.click();
		}
	};

	var closeBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	closeBtn.className = 'geBtn';

	tb.appendChild(fileInfo);
	tb.appendChild(compareBtn);
	tb.appendChild(zoomOutBtn);
	tb.appendChild(zoomFitBtn);
	tb.appendChild(zoomInBtn);
	tb.appendChild(pageSelect);
	tb.appendChild(mergeBtn);

	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(closeBtn);
	}

	buttons.appendChild(newBtn);

	if (file != null && file.constructor == DriveFile)
	{
		buttons.appendChild(createBtn);
	}

	buttons.appendChild(restoreBtn);

	if (!editorUi.editor.cancelFirst)
	{
		buttons.appendChild(closeBtn);
	}

	div.appendChild(buttons);
	div.appendChild(tb);

	this.container = div;
};

/**
 * Constructs a new revision dialog
 */
var DraftDialog = function(editorUi, title, xml, editFn, discardFn, editLabel, discardLabel, ignoreFn, drafts)
{
	var div = document.createElement('div');
	
	var titleDiv = document.createElement('div');
	titleDiv.style.marginTop = '0px';
	titleDiv.style.whiteSpace = 'nowrap';
	titleDiv.style.overflow = 'auto';
	titleDiv.style.lineHeight = 'normal';
	mxUtils.write(titleDiv, title);
	div.appendChild(titleDiv);
	
	var select = document.createElement('select');
	
	var draftSelected = mxUtils.bind(this, function()
	{
		if (select.value == '-1')
		{
			select.value = select.options[0].value;
			draftSelected();

			// Discard all drafts
			editorUi.confirm(mxResources.get('areYouSure'), null, mxUtils.bind(this, async function()
			{
				for (var i = 0; i < drafts.length; i++)
				{
					discardFn.apply(this, [i, mxUtils.bind(this, function()
					{
						// Do nothing
					})]);
				}

				editorUi.hideDialog(true);
			}), mxResources.get('no'), mxResources.get('yes'));
		}
		else
		{
			doc = mxUtils.parseXml(drafts[select.value].data);
			node = editorUi.editor.extractGraphModel(doc.documentElement, true);
			currentPage = 0;
				
			this.init();
		}
	});
	
	if (drafts != null)
	{
		select.style.marginLeft = '4px';
		
		for (var i = 0; i < drafts.length; i++)
		{
			var opt = document.createElement('option');
			opt.setAttribute('value', i);
			var ts0 = new Date(drafts[i].created);
			var ts1 = new Date(drafts[i].modified);
			
			mxUtils.write(opt, ts0.toLocaleDateString() + ' ' +
				ts0.toLocaleTimeString() + ' - ' +
				((ts0.toDateString() != ts1.toDateString() || true) ?
				ts1.toLocaleDateString() : ' ') +
				' ' + ts1.toLocaleTimeString());
			
			select.appendChild(opt);
		}

		// Delete all option
		var opt = document.createElement('option');
		opt.setAttribute('value', '-1');
		mxUtils.write(opt, mxResources.get('deleteAll'));
		select.appendChild(opt);
		titleDiv.appendChild(select);
		mxEvent.addListener(select, 'change', draftSelected);
	}
	
	if (xml == null)
	{
		xml = drafts[0].data;
	}
	
	var container = document.createElement('div');
	container.style.position = 'absolute';
	container.style.border = '1px solid lightGray';
	container.style.marginTop = '10px';
	container.style.left = '40px';
	container.style.right = '40px';
	container.style.top = '46px';
	container.style.bottom = '74px';
	container.style.overflow = 'hidden';
	
	mxEvent.disableContextMenu(container);
	div.appendChild(container);

	var graph = new Graph(container);
	graph.setEnabled(false);
	graph.setPanning(true);
	graph.shapeBackgroundColor = 'light-dark(#ffffff, #2a252f)';
	graph.panningHandler.ignoreCell = true;
	graph.panningHandler.useLeftButtonForPanning = true;
	graph.minFitScale = null;
	graph.maxFitScale = null;
	graph.centerZoom = true;
	
	// Handles placeholders for pages
	var doc = mxUtils.parseXml(xml);
	var node = editorUi.editor.extractGraphModel(doc.documentElement, true);
	var currentPage = 0;
	var diagrams = null;
	var graphGetGlobalVariable = graph.getGlobalVariable;

	graph.getGlobalVariable = function(name)
	{
		if (name == 'page' && diagrams != null && diagrams[currentPage] != null)
		{
			return diagrams[currentPage].getAttribute('name');
		}
		else if (name == 'pagenumber')
		{
			return currentPage + 1;
		}
		else if (name == 'pagecount')
		{
			return (diagrams != null) ? diagrams.length : 1;
		}
		
		return graphGetGlobalVariable.apply(this, arguments);
	};
	
	// Disables hyperlinks
	graph.getLinkForCell = function()
	{
		return null;
	};
	
	var zoomInBtn = editorUi.createToolbarButton(Editor.zoomInImage, mxResources.get('zoomIn'), function()
	{
		graph.zoomIn();
	}, 20);

	var zoomOutBtn = editorUi.createToolbarButton(Editor.zoomOutImage, mxResources.get('zoomOut'), function()
	{
		graph.zoomOut();
	}, 20);

	var zoomFitBtn = editorUi.createToolbarButton(Editor.zoomFitImage, mxResources.get('fit'), function()
	{
		if (graph.view.scale == 1)
		{
			graph.maxFitScale = 8;
			graph.fit(8);
		}
		else
		{
			graph.zoomActual();
		}

		graph.center();
	}, 20);

	var restoreBtn = mxUtils.button(discardLabel || mxResources.get('discard'), function()
	{
		editorUi.confirm(mxResources.get('areYouSure'), null, mxUtils.bind(this, async function()
		{
			discardFn.apply(this, [select.value, mxUtils.bind(this, function()
			{
				if (select.parentNode != null)
				{
					select.options[select.selectedIndex].parentNode.removeChild(select.options[select.selectedIndex]);
					
					if (select.options.length > 1)
					{
						select.value = select.options[0].value;
						draftSelected();
					}
					else
					{
						editorUi.hideDialog(true);
					}
				}
			})]);
		}), mxResources.get('no'), mxResources.get('yes'));
	});
	restoreBtn.className = 'geBtn';
	
	var pageSelect = document.createElement('select');
	pageSelect.style.maxWidth = '80px';
	pageSelect.style.position = 'relative';
	pageSelect.style.top = '-2px';
	pageSelect.style.verticalAlign = 'bottom';
	pageSelect.style.marginRight = '6px';
	pageSelect.style.display = 'none';

	var showBtn = mxUtils.button(editLabel || mxResources.get('edit'), function()
	{
		editFn.apply(this, [select.value])
	});
	showBtn.className = 'geBtn gePrimaryBtn';

	var buttons = document.createElement('div');
	buttons.style.position = 'absolute';
	buttons.style.bottom = '30px';
	buttons.style.right = '40px';
	buttons.style.textAlign = 'right';

	var tb = document.createElement('div');
	tb.className = 'geToolbarContainer';
	tb.style.cssText = 'box-shadow:none !important;background-color:transparent;' +
		'padding:2px;border-style:none !important;bottom:30px;';

	this.init = function()
	{
		function parseGraphModel(dataNode)
		{
			if (dataNode != null)
			{
				var bg = dataNode.getAttribute('background');
				
				if (bg == null || bg == '' || bg == mxConstants.NONE)
				{
					bg = 'light-dark(#ffffff, transparent)';
				}
				
				container.style.backgroundColor = bg;
				
				var codec = new mxCodec(dataNode.ownerDocument);
				codec.decode(dataNode, graph.getModel());
				graph.maxFitScale = 1;
				graph.fit(8);
				graph.center();
			}
			
			return dataNode;
		};
			
		function parseDiagram(diagramNode)
		{
			if (diagramNode != null)
			{
				diagramNode = parseGraphModel(Editor.parseDiagramNode(diagramNode));
			}
			
			return diagramNode;
		};

		mxEvent.addListener(pageSelect, 'change', function(evt)
		{
			currentPage = parseInt(pageSelect.value);
			parseDiagram(diagrams[currentPage]);
			mxEvent.consume(evt);
		});
		
		if (node.nodeName == 'mxfile')
		{
			// Workaround for "invalid calling object" error in IE
			var tmp = node.getElementsByTagName('diagram');
			diagrams = [];
			
			for (var i = 0; i < tmp.length; i++)
			{
				diagrams.push(tmp[i]);	
			}
			
			if (diagrams.length > 0)
			{
				parseDiagram(diagrams[currentPage]);
			}

			pageSelect.innerText = '';
			
			if (diagrams.length > 1)
			{
				pageSelect.style.display = '';
	
				for (var i = 0; i < diagrams.length; i++)
				{
					var pageOption = document.createElement('option');
					mxUtils.write(pageOption, diagrams[i].getAttribute('name') ||
						mxResources.get('pageWithNumber', [i + 1]));
					pageOption.setAttribute('value', i);
					
					if (i == currentPage)
					{
						pageOption.setAttribute('selected', 'selected');
					}
	
					pageSelect.appendChild(pageOption);
				}
			}
			else
			{
				pageSelect.style.display = 'none';
			}
		}
		else
		{
			parseGraphModel(node);
		}
	};
	
	tb.appendChild(pageSelect);
	tb.appendChild(zoomInBtn);
	tb.appendChild(zoomOutBtn);
	tb.appendChild(zoomFitBtn);
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog(true);
	});
	
	cancelBtn.className = 'geBtn';
	
	var ignoreBtn = (ignoreFn != null) ? mxUtils.button(mxResources.get('ignore'), ignoreFn) : null;
	
	if (ignoreBtn != null)
	{
		ignoreBtn.className = 'geBtn';
	}

	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
		
		if (ignoreBtn != null)
		{
			buttons.appendChild(ignoreBtn);
		}
		
		buttons.appendChild(restoreBtn);
		buttons.appendChild(showBtn);
	}
	else
	{
		buttons.appendChild(showBtn);
		buttons.appendChild(restoreBtn);
		
		if (ignoreBtn != null)
		{
			buttons.appendChild(ignoreBtn);
		}
		
		buttons.appendChild(cancelBtn);
	}

	div.appendChild(buttons);
	div.appendChild(tb);

	this.container = div;
};

/**
 * 
 */
var FindWindow = function(ui, x, y, w, h, withReplace)
{
	var action = ui.actions.get('findReplace');
	
	var graph = ui.editor.graph;
	var lastSearch = null;
	var lastFound = null;
	var lastSearchSuccessful = false;
	var allChecked = false;
	var lblMatch = null;
	var lblMatchPos = 0;
	var marker = 1;
	
	var div = document.createElement('div');
	div.style.userSelect = 'none';
	div.style.overflow = 'hidden';
	div.style.padding = '10px';
	div.style.height = '100%';
	
	var txtWidth = withReplace? '260px' : '200px';
	var searchInput = document.createElement('input');
	searchInput.setAttribute('placeholder', mxResources.get('find'));
	searchInput.setAttribute('type', 'text');
	searchInput.style.marginTop = '4px';
	searchInput.style.marginBottom = '6px';
	searchInput.style.width = txtWidth;
	searchInput.style.fontSize = '12px';
	searchInput.style.borderRadius = '4px';
	searchInput.style.padding = '6px';
	div.appendChild(searchInput);
	mxUtils.br(div);

	var replaceInput;
	
	if (withReplace)
	{
		replaceInput = document.createElement('input');
		replaceInput.setAttribute('placeholder', mxResources.get('replaceWith'));
		replaceInput.setAttribute('type', 'text');
		replaceInput.style.marginTop = '4px';
		replaceInput.style.marginBottom = '6px';
		replaceInput.style.width = txtWidth;
		replaceInput.style.fontSize = '12px';
		replaceInput.style.borderRadius = '4px';
		replaceInput.style.padding = '6px';
		div.appendChild(replaceInput);
		mxUtils.br(div);
		
		mxEvent.addListener(replaceInput, 'input', updateReplBtns);
	}
	
	var regexInput = document.createElement('input');
	regexInput.setAttribute('id', 'geFindWinRegExChck');
	regexInput.setAttribute('type', 'checkbox');
	regexInput.style.marginRight = '4px';
	div.appendChild(regexInput);
	
	var regexLabel = document.createElement('label');
	regexLabel.setAttribute('for', 'geFindWinRegExChck');
	div.appendChild(regexLabel);
	mxUtils.write(regexLabel, mxResources.get('regularExpression'));
	div.appendChild(regexLabel);
	
    var help = ui.menus.createHelpLink('https://www.drawio.com/doc/faq/find-shapes');
    help.style.position = 'relative';
    help.style.marginLeft = '6px';
    div.appendChild(help);
    
	mxUtils.br(div);

    var allPagesInput = document.createElement('input');
    allPagesInput.setAttribute('id', 'geFindWinAllPagesChck');
    allPagesInput.setAttribute('type', 'checkbox');
    allPagesInput.style.marginRight = '4px';
	div.appendChild(allPagesInput);
	
	var allPagesLabel = document.createElement('label');
	allPagesLabel.setAttribute('for', 'geFindWinAllPagesChck');
	div.appendChild(allPagesLabel);
	mxUtils.write(allPagesLabel, mxResources.get('allPages'));
	div.appendChild(allPagesLabel);
    
	var tmp = document.createElement('div');
	
	function testMeta(re, cell, search)
	{
		if (typeof cell.value === 'object' && cell.value.attributes != null)
		{
			var attrs = cell.value.attributes;
			
			for (var i = 0; i < attrs.length; i++)
			{
				if (attrs[i].nodeName != 'label')
				{
					var value = mxUtils.trim(attrs[i].nodeValue.replace(/[\x00-\x1F\x7F-\x9F]|\s+/g, ' ')).toLowerCase();
					
					if ((re == null && value.indexOf(search) >= 0) ||
						(re != null && re.test(value)))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	};
	
	function updateReplBtns()
	{
		if (lastSearchSuccessful)
		{
			replaceFindBtn.removeAttribute('disabled');
			replaceBtn.removeAttribute('disabled');
		}
		else
		{
			replaceFindBtn.setAttribute('disabled', 'disabled');
			replaceBtn.setAttribute('disabled', 'disabled');
		}
		
		if (searchInput.value)
		{
			replaceAllBtn.removeAttribute('disabled');
		}
		else
		{
			replaceAllBtn.setAttribute('disabled', 'disabled');
		}
	}
				
	function search(internalCall, trySameCell, stayOnPage)
	{
		replAllNotif.innerText = '';
		var cells = graph.model.getDescendants(graph.model.getRoot());
		var searchStr = searchInput.value.toLowerCase();
		var re = (regexInput.checked) ? new RegExp(searchStr) : null;
		var firstMatch = null;
		lblMatch = null;
		
		if (lastSearch != searchStr)
		{
			lastSearch = searchStr;
			lastFound = null;
			allChecked = false;
		}

		var active = lastFound == null;
		
		if (searchStr.length > 0)
		{
			if (allChecked)
			{
				allChecked = false;
				
				//Find current page index
				var currentPageIndex;
				
				for (var i = 0; i < ui.pages.length; i++)
				{
					if (ui.currentPage == ui.pages[i])
					{
						currentPageIndex = i;
						break;
					}
				}
				
				var nextPageIndex = (currentPageIndex + 1) % ui.pages.length, nextPage;
				lastFound = null;
				
				do
				{
					allChecked = false;
					nextPage = ui.pages[nextPageIndex];
					graph = ui.createTemporaryGraph(graph.getStylesheet());
					ui.updatePageRoot(nextPage);
					graph.model.setRoot(nextPage.root);
					nextPageIndex = (nextPageIndex + 1) % ui.pages.length;
				}
				while(!search(true, trySameCell, stayOnPage) && nextPageIndex != currentPageIndex);
				
				if (lastFound)
				{
					lastFound = null;
					
					if (!stayOnPage)
					{
						ui.selectPage(nextPage);
					}
					else
					{
						ui.editor.graph.model.execute(new SelectPage(ui, nextPage));
					}
				}
				
				allChecked = false;
				graph = ui.editor.graph;
				
				return search(true, trySameCell, stayOnPage);
			}
			
			var i;
			
			for (i = 0; i < cells.length; i++)
			{
				var state = graph.view.getState(cells[i]);
				
				//Try the same cell with replace to find other occurances
				if (trySameCell)
				{
					active = active || state == lastFound;
				}
							
				if (state != null && state.cell.value != null && (active || firstMatch == null) &&
					(graph.model.isVertex(state.cell) || graph.model.isEdge(state.cell)))
				{
					if (state.style != null && state.style['html'] == '1')
					{
						tmp.innerHTML = graph.sanitizeHtml(graph.getLabel(state.cell));
						label = mxUtils.extractTextWithWhitespace([tmp]);
					}
					else
					{
						label = graph.getLabel(state.cell);
					}
		
					label = mxUtils.trim(label.replace(/[\x00-\x1F\x7F-\x9F]|\s+/g, ' ')).toLowerCase();
					var lblPosShift = 0;
					
					if (trySameCell && withReplace && state == lastFound)
					{
						label = label.substr(lblMatchPos);
						lblPosShift = lblMatchPos;
					}
					
					var checkMeta = replaceInput == null || replaceInput.value == '';
					
					if ((re == null && ((label.indexOf(searchStr) >= 0) ||
						(checkMeta && testMeta(re, state.cell, searchStr)))) ||
						(re != null && (re.test(label) || (checkMeta &&
						testMeta(re, state.cell, searchStr)))))
					{
						if (withReplace)
						{
							if (re != null)
							{
								var result = label.match(re);

								if (result != null && result.length > 0)
								{
									lblMatch = result[0].toLowerCase();
									lblMatchPos = lblPosShift + result.index + lblMatch.length;
								}
							}
							else
							{
								lblMatch = searchStr;
								lblMatchPos = lblPosShift + label.indexOf(searchStr) + lblMatch.length;
							} 	
						}
						
						if (active)
						{
							firstMatch = state;
						
							break;
						}
						else if (firstMatch == null)
						{
							firstMatch = state;
						}
					}
				}
	
				active = active || state == lastFound;
			}
		}
					
		if (firstMatch != null)
		{
			if (i == cells.length && allPagesInput.checked)
			{
				lastFound = null;
				allChecked = true;
				return search(true, trySameCell, stayOnPage);
			}
			
			lastFound = firstMatch;
			graph.scrollCellToVisible(lastFound.cell);
			
			if (graph.isEnabled() && !graph.isCellLocked(lastFound.cell))
			{
				if (!stayOnPage &&
					(graph.getSelectionCell() != lastFound.cell ||
					graph.getSelectionCount() != 1))
				{
					graph.setSelectionCell(lastFound.cell);
				}
			}
			else
			{
				graph.highlightCell(lastFound.cell);
			}
		}
		//Check other pages
		else if (!internalCall && allPagesInput.checked)
		{
			allChecked = true;
			return search(true, trySameCell, stayOnPage);
		}
		else if (graph.isEnabled() && !stayOnPage)
		{
			graph.clearSelection();
		}
		
		lastSearchSuccessful = firstMatch != null;
		
		if (withReplace && !internalCall)
		{
			updateReplBtns();
		}
		
		return searchStr.length == 0 || firstMatch != null;
	};

	mxUtils.br(div);
	
	var btnsCont = document.createElement('div');
	btnsCont.style.left = '0px';
	btnsCont.style.right = '0px';
	btnsCont.style.marginTop = '6px';
	btnsCont.style.padding = '0 6px 0 6px';
	div.appendChild(btnsCont);

	var resetBtn = mxUtils.button(mxResources.get('reset'), function()
	{
		replAllNotif.innerText = '';
		searchInput.value = '';
		searchInput.style.backgroundColor = '';
		
		if (withReplace)
		{
			replaceInput.value = '';
			updateReplBtns();
		}
		
		lastFound = null;
		lastSearch = null;
		allChecked = false;
		searchInput.focus();
	});
	
	resetBtn.setAttribute('title', mxResources.get('reset'));
	resetBtn.style.float = 'none';
	resetBtn.style.width = '120px';
	resetBtn.style.marginTop = '6px';
	resetBtn.style.marginLeft = '8px';
	resetBtn.style.overflow = 'hidden';
	resetBtn.style.textOverflow = 'ellipsis';
	resetBtn.className = 'geBtn';
	
	if (!withReplace)
	{
		btnsCont.appendChild(resetBtn);		
	}

	var btn = mxUtils.button(mxResources.get('find'), function()
	{
		try
		{
			searchInput.style.backgroundColor = search() ? '' :
				'light-dark(#ffcfcf, #ff0000)';
		}
		catch (e)
		{
			ui.handleError(e);	
		}
	});
	
	// TODO: Reset state after selection change
	btn.setAttribute('title', mxResources.get('find') + ' (Enter)');
	btn.style.float = 'none';
	btn.style.width = '120px';
	btn.style.marginTop = '6px';
	btn.style.marginLeft = '8px';
	btn.style.overflow = 'hidden';
	btn.style.textOverflow = 'ellipsis';
	btn.className = 'geBtn gePrimaryBtn';
	
	btnsCont.appendChild(btn);

	var replAllNotif = document.createElement('div');
	replAllNotif.style.marginTop = '10px';
	
	if (!withReplace)
	{
		btnsCont.style.display = 'flex';
		btnsCont.style.alignItems = 'center';
		resetBtn.style.width = '';	
		btn.style.width = '';
	}
	else
	{
		btnsCont.style.textAlign = 'center';

		function replaceInLabel(str, substr, newSubstr, startIndex, style)
		{
			if (style == null || style['html'] != '1')
			{
				var replStart = str.toLowerCase().indexOf(substr, startIndex);
				return replStart < 0? str : str.substr(0, replStart) + newSubstr + str.substr(replStart + substr.length);
			}
			
			var origStr = str;
			substr = mxUtils.htmlEntities(substr, false, false, false);
			var tagPos = [], p = -1;
			
			//Original position (startIndex) counts for \n which is removed when tags are removed, so handle <br> separately
			// The same for block level elements which are replaced by \n
			str = str.replace(/<br>/ig, '\n').replace(/(\s|\S)(<(BLOCKQUOTE|DIV|H1|H2|H3|H4|H5|H6|OL|P|PRE|TABLE|UL)[^>]*>)/ig, '$1\n$2');

			while((p = str.indexOf('<', p + 1)) > -1)
			{
				tagPos.push(p);
			}
			
			var tags = str.match(/<[^>]*>/g);
			str = str.replace(/<[^>]*>/g, '');
			var lStr = str.toLowerCase();
			var replStart = lStr.indexOf(substr, startIndex);
			
			if (replStart < 0)
			{
				return origStr;	
			}
			
			var replEnd = replStart + substr.length;
			var newSubstr = mxUtils.htmlEntities(newSubstr);
			
			//Tags within the replaced text is added before it
			var newStr = str.substr(0, replStart) + newSubstr + str.substr(replEnd);
			var tagDiff = 0;
			
			for (var i = 0; i < tagPos.length; i++)
			{
				if (tagPos[i] - tagDiff < replStart)
				{
					newStr = newStr.substr(0, tagPos[i]) + tags[i] + newStr.substr(tagPos[i]);
				}
				else if (tagPos[i] - tagDiff < replEnd)
				{
					var inPos = replStart + tagDiff;
					newStr = newStr.substr(0, inPos) + tags[i] + newStr.substr(inPos);
				}
				else
				{
					var inPos = tagPos[i] + (newSubstr.length - substr.length);
					newStr = newStr.substr(0, inPos) + tags[i] + newStr.substr(inPos);
				}
				
				tagDiff += tags[i].length;
			}
			
			return newStr.replace(/\n(<(BLOCKQUOTE|DIV|H1|H2|H3|H4|H5|H6|OL|P|PRE|TABLE|UL)[^>]*>)/ig, '$1').replace(/\n/g, '<br>');
		};
		
		var replaceFindBtn = mxUtils.button(mxResources.get('replFind'), function()
		{
			try
			{
				if (lblMatch != null && lastFound != null)
				{
					var cell = lastFound.cell, lbl = graph.getLabel(cell);
					
					if (graph.isCellEditable(cell))
					{
						graph.model.setValue(cell, replaceInLabel(lbl, lblMatch, replaceInput.value,
							lblMatchPos - lblMatch.length, graph.getCurrentCellStyle(cell)));
					}
					
					searchInput.style.backgroundColor = search(false, true) ? '' :
						'light-dark(#ffcfcf, #ff0000)';
				}
			}
			catch (e)
			{
				ui.handleError(e);
			}
		});
		
		replaceFindBtn.setAttribute('title', mxResources.get('replFind'));
		replaceFindBtn.style.float = 'none';
		replaceFindBtn.style.width = '120px';
		replaceFindBtn.style.marginTop = '6px';
		replaceFindBtn.style.marginLeft = '8px';
		replaceFindBtn.style.overflow = 'hidden';
		replaceFindBtn.style.textOverflow = 'ellipsis';
		replaceFindBtn.className = 'geBtn gePrimaryBtn';
		replaceFindBtn.setAttribute('disabled', 'disabled');
		
		btnsCont.appendChild(replaceFindBtn);
		mxUtils.br(btnsCont);
		
		var replaceBtn = mxUtils.button(mxResources.get('replace'), function()
		{
			try
			{
				if (lblMatch != null && lastFound != null)
				{
					var cell = lastFound.cell, lbl = graph.getLabel(cell);
					
					graph.model.setValue(cell, replaceInLabel(lbl, lblMatch, replaceInput.value,
						lblMatchPos - lblMatch.length, graph.getCurrentCellStyle(cell)));
					replaceFindBtn.setAttribute('disabled', 'disabled');
					replaceBtn.setAttribute('disabled', 'disabled');
				}
			}
			catch (e)
			{
				ui.handleError(e);	
			}
		});
		
		replaceBtn.setAttribute('title', mxResources.get('replace'));
		replaceBtn.style.float = 'none';
		replaceBtn.style.width = '120px';
		replaceBtn.style.marginTop = '6px';
		replaceBtn.style.marginLeft = '8px';
		replaceBtn.style.overflow = 'hidden';
		replaceBtn.style.textOverflow = 'ellipsis';
		replaceBtn.className = 'geBtn gePrimaryBtn';
		replaceBtn.setAttribute('disabled', 'disabled');
		
		btnsCont.appendChild(replaceBtn);
		
		var replaceAllBtn = mxUtils.button(mxResources.get('replaceAll'), function()
		{
			replAllNotif.innerText = '';
			
			lastSearch = null; // Reset last search to check all matches
			var currentPage = ui.currentPage;
			var cells = ui.editor.graph.getSelectionCells();
			ui.editor.graph.rendering = false;
			
			graph.getModel().beginUpdate();
			try
			{
				var safeguard = 0;
				var seen = {};
				
				while (search(false, true, true) && safeguard < 100)
				{
					var cell = lastFound.cell, lbl = graph.getLabel(cell);
					var oldSeen = seen[cell.id];
					
					if (oldSeen && oldSeen.replAllMrk == marker && oldSeen.replAllPos >= lblMatchPos)
					{
						break;
					}
					
					seen[cell.id] = {replAllMrk: marker, replAllPos: lblMatchPos};
					
					if (graph.isCellEditable(cell))
					{
						graph.model.setValue(cell, replaceInLabel(lbl, lblMatch, replaceInput.value,
							lblMatchPos - lblMatch.length, graph.getCurrentCellStyle(cell)));
						safeguard++;
					}
				}
				
				if (currentPage != ui.currentPage)
				{
					ui.editor.graph.model.execute(new SelectPage(ui, currentPage));
				}
				
				mxUtils.write(replAllNotif, mxResources.get('matchesRepl', [safeguard]));
			}
			catch (e)
			{
				ui.handleError(e);
			}
			finally
			{
				graph.getModel().endUpdate();
				ui.editor.graph.setSelectionCells(cells);
				ui.editor.graph.rendering = true;
			}
			
			marker++;
		});
		
		replaceAllBtn.setAttribute('title', mxResources.get('replaceAll'));
		replaceAllBtn.style.float = 'none';
		replaceAllBtn.style.width = '120px';
		replaceAllBtn.style.marginTop = '6px';
		replaceAllBtn.style.marginLeft = '8px';
		replaceAllBtn.style.overflow = 'hidden';
		replaceAllBtn.style.textOverflow = 'ellipsis';
		replaceAllBtn.className = 'geBtn gePrimaryBtn';
		replaceAllBtn.setAttribute('disabled', 'disabled');
		
		btnsCont.appendChild(replaceAllBtn);
		mxUtils.br(btnsCont);
		btnsCont.appendChild(resetBtn);		

		var closeBtn = mxUtils.button(mxResources.get('close'), mxUtils.bind(this, function()
		{
			this.window.setVisible(false);
		}));
		
		closeBtn.setAttribute('title', mxResources.get('close'));
		closeBtn.style.float = 'none';
		closeBtn.style.width = '120px';
		closeBtn.style.marginTop = '6px';
		closeBtn.style.marginLeft = '8px';
		closeBtn.style.overflow = 'hidden';
		closeBtn.style.textOverflow = 'ellipsis';
		closeBtn.className = 'geBtn';
		
		btnsCont.appendChild(closeBtn);
		mxUtils.br(btnsCont);
		btnsCont.appendChild(replAllNotif);
	}
	
	mxEvent.addListener(searchInput, 'keyup', function(evt)
	{
		// Ctrl or Cmd keys
		if (evt.keyCode == 91 || evt.keyCode == 93 || evt.keyCode == 17)
		{
			// Workaround for lost focus on show
			mxEvent.consume(evt);
		}
		else if (evt.keyCode == 27)
		{
			// Escape closes window
			action.funct();
		}
		else if (lastSearch != searchInput.value.toLowerCase() || evt.keyCode == 13)
		{
			try
			{
				searchInput.style.backgroundColor = search() ? '' :
					'light-dark(#ffcfcf, #ff0000)';
			}
			catch (e)
			{
				searchInput.style.backgroundColor = 'light-dark(#ffcfcf, #ff0000)';
			}
		}
	});

	// Ctrl+F closes window
	mxEvent.addListener(div, 'keydown', function(evt)
	{
		if (evt.keyCode == 70 && ui.keyHandler.isControlDown(evt) && !mxEvent.isShiftDown(evt))
		{
			action.funct();
			mxEvent.consume(evt);
		}
	});

	this.window = new mxWindow(mxResources.get('find') + ((withReplace) ?
		'/' + mxResources.get('replace') : ''),
		div, x, y, w, h, true, true);
	this.window.destroyOnClose = false;
	this.window.setMaximizable(false);
	this.window.setResizable(false);
	this.window.setClosable(true);
	
	this.window.addListener('show', mxUtils.bind(this, function()
	{
		this.window.fit();
		
		if (this.window.isVisible())
		{
			searchInput.focus();
			
			if (mxClient.IS_GC || mxClient.IS_FF)
			{
				searchInput.select();
			}
			else
			{
				document.execCommand('selectAll', false, null);
			}
			
			if (ui.pages != null && ui.pages.length > 1)
			{
				allPagesInput.removeAttribute('disabled');
			}
			else
			{
				allPagesInput.checked = false;
				allPagesInput.setAttribute('disabled', 'disabled');
			}
		}
		else
		{
			graph.container.focus();
		}
	}));
	
	ui.installResizeHandler(this, false);

	this.doSearch = function(searchTerms)
	{
		searchInput.focus();
		searchInput.value = searchTerms;
		btn.click();
	};
};

/**
 * 
 */
var FreehandWindow = function(editorUi, x, y, w, h, withBrush)
{
	var graph = editorUi.editor.graph;

	var div = document.createElement('div');
	div.style.textAlign = 'center';
	div.style.userSelect = 'none';
	div.style.overflow = 'hidden';
	div.style.height = '100%';
	
	if (withBrush)
	{
		var brushInput = document.createElement('input');
		brushInput.setAttribute('id', 'geFreehandBrush');
		brushInput.setAttribute('type', 'checkbox');
		brushInput.checked = graph.freehand.isPerfectFreehandMode();
		brushInput.style.margin = '10px 5px 0px 10px';
		brushInput.style.float = 'left';
		div.appendChild(brushInput);
		
		// Used to retrieve default styles
		graph.freehand.setPerfectFreehandMode(brushInput.checked);
		
		var brushLabel = document.createElement('label');
		brushLabel.setAttribute('for', 'geFreehandBrush');
		brushLabel.style.float = 'left';
		brushLabel.style.marginTop = '10px';
		div.appendChild(brushLabel);
		mxUtils.write(brushLabel, mxResources.get('brush'));
		div.appendChild(brushLabel);

		var tempDiv = document.createElement('tempDiv');
		tempDiv.style.display = 'block';
		tempDiv.style.width = '100%';
		tempDiv.style.height = '100%';
		tempDiv.style.borderRadius = '2px';
		tempDiv.style.boxSizing = 'border-box';
		tempDiv.style.border = '1px solid black';
		tempDiv.style.backgroundColor = graph.freehand.getStrokeColor();

		function updateName()
		{
			var color = graph.freehand.getStrokeColor(true);

			if (color != null && color != mxConstants.NONE &&
				color.length > 1 && typeof color === 'string')
			{
				var name = null;

				if (color == 'default')
				{
					name = mxResources.get('useBlackAndWhite');
				}
				else
				{
					var clr = (color.charAt(0) == '#') ?
						color.substring(1).toUpperCase() : color;
					name = ColorDialog.prototype.colorNames[clr];
				}

				if (name != null)
				{
					tempDiv.setAttribute('title', name);
				}
			}
		};

		editorUi.addListener('darkModeChanged', function()
		{
			tempDiv.style.backgroundColor = graph.freehand.getStrokeColor();
		});
		
		updateName();

		var btn = mxUtils.button('', mxUtils.bind(this, function(evt)
		{
			editorUi.pickColor(graph.freehand.getStrokeColor(true), function(newColor)
			{
				graph.freehand.setStrokeColor(newColor);
				tempDiv.style.backgroundColor = graph.freehand.getStrokeColor();
				updateName();
			}, 'default');
			
			mxEvent.consume(evt);
		}));
		
		btn.style.position = 'absolute';
		btn.style.boxSizing = 'border-box';
		btn.style.padding = '2px';
		btn.style.top = '8px';
		btn.style.right = '8px';
		btn.style.width = '28px';
		btn.style.height = '18px';
		btn.className = 'geColorBtn';
		btn.innerText = '';
		btn.appendChild(tempDiv);
		div.appendChild(btn);

		var settings = document.createElement('img');
		settings.setAttribute('title', mxResources.get('settings'));
		settings.setAttribute('src', Editor.gearImage);
		settings.className = 'geButton';
		settings.style.position = 'absolute';
		settings.style.boxSizing = 'border-box';
		settings.style.padding = '2px';
		settings.style.top = '8px';
		settings.style.right = '38px';
		settings.style.width = '18px';
		settings.style.height = '18px';
		settings.style.opacity = '0.6';
		div.appendChild(settings);

		mxEvent.addListener(settings, 'click', mxUtils.bind(this, function(evt)
		{
			var smoothing = graph.freehand.getSmoothing();

			editorUi.prompt(mxResources.get('smoothing') + ' (1-20)', smoothing, function(newValue)
			{
				if (!isNaN(newValue) && newValue > 0 && newValue <= 20)
				{
					graph.freehand.setSmoothing(parseInt(newValue));
				}
			});
		}));

		mxUtils.br(div);

		var brushSize = document.createElement('input');
		brushSize.setAttribute('type', 'range');
		brushSize.setAttribute('min', '2');
		brushSize.setAttribute('max', '30');
		brushSize.setAttribute('value', graph.freehand.getBrushSize());
		brushSize.style.width = '90%';
		brushSize.style.visibility = 'hidden';
		div.appendChild(brushSize);
		mxUtils.br(div);

		var updateBrushState = function()
		{
			graph.freehand.setPerfectFreehandMode(brushInput.checked)
			brushSize.style.visibility = brushInput.checked? 'visible' : 'hidden';
		};

		mxEvent.addListener(brushInput, 'change', updateBrushState);
		updateBrushState();

		mxEvent.addListener(brushSize, 'change', function()
		{
			graph.freehand.setBrushSize(parseInt(this.value));
		});
	}
	
	var startBtn = mxUtils.button(mxResources.get('startDrawing'), function()
	{
		if (graph.freehand.isDrawing())
		{
			graph.freehand.stopDrawing();
		}
		else
		{
			graph.freehand.startDrawing();
		}
	});
	
	startBtn.setAttribute('title', mxResources.get('startDrawing') + ' (X)');
	startBtn.style.width = '90%';
	startBtn.style.marginLeft = '0px';
	startBtn.style.position = 'relative';
	startBtn.className = 'geBtn gePrimaryBtn';
	
	div.appendChild(startBtn);

	this.window = new mxWindow(mxResources.get('freehand'), div, x, y, w, h, true, true);
	this.window.destroyOnClose = false;
	this.window.setMaximizable(false);
	this.window.setResizable(false);
	this.window.setClosable(true);
	
	graph.addListener('freehandStateChanged', mxUtils.bind(this, function()
	{
		startBtn.innerText = '';
		mxUtils.write(startBtn, mxResources.get(graph.freehand.isDrawing() ? 'stopDrawing' : 'startDrawing'));

		var shortcut = document.createElement('span');
		shortcut.className = 'geShortcutKey';
		shortcut.style.margin = '0 2px 4px 0';
		mxUtils.write(shortcut, 'X');
		startBtn.appendChild(shortcut);

		startBtn.setAttribute('title', mxResources.get(graph.freehand.isDrawing() ? 'stopDrawing' : 'startDrawing') + ' (X)');
		startBtn.className = 'geBtn' + (graph.freehand.isDrawing() ? ' gePrimaryBtn' : '');
	}));
	
	this.window.addListener('show', mxUtils.bind(this, function()
	{
		this.window.fit();
	}));
	
	this.window.addListener('hide', mxUtils.bind(this, function()
	{
		if (graph.freehand.isDrawing())
		{
			graph.freehand.stopDrawing();
		}
	}));
	
	editorUi.installResizeHandler(this, false);
};

/**
 * 
 */
var AdaptiveColorsWindow = function(editorUi, x, y, w, h)
{
	var graph = editorUi.editor.graph;

	var div = document.createElement('div');
	div.style.userSelect = 'none';
	div.style.overflow = 'hidden';
	div.style.height = '100%';

	var section = document.createElement('div');
	section.style.display = 'flex';
	section.style.alignItems = 'center';
	section.style.justifyContent = 'center';
	section.style.paddingTop = '20px';

	var labelCheckbox = document.createElement('input');
	labelCheckbox.setAttribute('type', 'checkbox');
	labelCheckbox.style.marginRight = '4px';
	labelCheckbox.checked = true;

	var backgroundCheckbox = document.createElement('input');
	backgroundCheckbox.setAttribute('type', 'checkbox');
	backgroundCheckbox.style.marginRight = '4px';
	backgroundCheckbox.checked = true;

	var btn = mxUtils.button(mxResources.get('removeIt', [mxResources.get('userDefined')]), mxUtils.bind(this, function()
	{
		editorUi.removeUserDefinedDarkColors((graph.isSelectionEmpty()) ?
			graph.getVerticesAndEdges() : graph.getSelectionCells(),
			labelCheckbox.checked, backgroundCheckbox.checked);
	}));

	btn.setAttribute('title', 'Convert Colors');
	btn.className = 'geBtn gePrimaryBtn';
	section.appendChild(btn);
	div.appendChild(section);

	section = section.cloneNode(false);
	section.appendChild(backgroundCheckbox);
	section.style.paddingTop = '8px';

	mxUtils.write(section, mxResources.get('background'));
	div.appendChild(section);

	section = section.cloneNode(false);
	section.appendChild(labelCheckbox);
	section.style.paddingTop = '8px';
	
	mxUtils.write(section, mxResources.get('labels'));
	div.appendChild(section);
	
	this.window = new mxWindow(mxResources.get('adaptiveColors'), div, x, y, w, h, true, true);
	this.window.destroyOnClose = false;
	this.window.setMinimizable(false);
	this.window.setMaximizable(false);
	this.window.setResizable(false);
	this.window.setClosable(true);
	
	this.window.addListener('show', mxUtils.bind(this, function()
	{
		this.window.fit();
	}));
	
	editorUi.installResizeHandler(this, false);
};

/**
 * 
 */
var ChatWindow = function(editorUi, x, y, w, h)
{
	var graph = editorUi.editor.graph;

	var div = document.createElement('div');
	div.style.display = 'flex';
	div.style.flexDirection = 'column';
	div.style.overflow = 'hidden';
	div.style.height = '100%';
	div.style.padding = '10px 12px 20px 12px';
	div.style.boxSizing = 'border-box';

	mxEvent.addGestureListeners(div, mxUtils.bind(this, function(evt)
	{
		if (editorUi.sidebar != null)
		{
			editorUi.sidebar.hideTooltip();
		}
	}), null, null);

	var hist = document.createElement('div');
	hist.style.flexGrow = '1';
	hist.style.overflow = 'auto';
	hist.style.fontSize = '12px';
	hist.style.marginRight = '-8px';
	hist.style.paddingRight = '8px';

	mxEvent.addListener(hist, 'scroll', function()
	{
		if (editorUi.sidebar != null)
		{
			editorUi.sidebar.hideTooltip();
		}
	});
	
	div.appendChild(hist);

	var user = document.createElement('div');
	user.style.borderRadius = '20px';
	user.style.backgroundColor = 'light-dark(#e0e0e0, #3a3a3a)';
	user.style.padding = '8px';
	user.style.marginTop = '8px';

	var options = document.createElement('div');
	options.style.display = 'flex';
	options.style.gap = '6px';
	options.style.paddingRight = '8px';
	options.style.justifyContent = 'start';

	var typeSelect = document.createElement('select');
	typeSelect.style.borderColor = 'transparent';
	typeSelect.style.textOverflow = 'ellipsis';
	typeSelect.style.padding = '5px';
	typeSelect.style.minWidth = '0';

	var createPublicOption = document.createElement('option');
	
	if (typeof mxMermaidToDrawio !== 'undefined' && window.isMermaidEnabled &&
		mxUtils.indexOf(Editor.aiActions, 'createPublic') >= 0)
	{
		createPublicOption.setAttribute('value', 'createPublic');
		mxUtils.write(createPublicOption, mxResources.get('create') +
			' (' + mxResources.get('draw.io') + ')');
		typeSelect.appendChild(createPublicOption);
	}

	var includeOption = document.createElement('option');
	var selectionOption = document.createElement('option');
	var createOption = document.createElement('option');
	var helpOption = document.createElement('option');

	if (typeof mxMermaidToDrawio !== 'undefined' && window.isMermaidEnabled &&
		mxUtils.indexOf(Editor.aiActions, 'create') >= 0)
	{
		createOption.setAttribute('value', 'create');
		mxUtils.write(createOption, mxResources.get('create'));
		typeSelect.appendChild(createOption);
	}

	if (mxUtils.indexOf(Editor.aiActions, 'update') >= 0)
	{
		includeOption.setAttribute('value', 'includeCopyOfMyDiagram');
		mxUtils.write(includeOption, mxResources.get('includeCopyOfMyDiagram'));
		typeSelect.appendChild(includeOption);

		selectionOption.setAttribute('value', 'selectionOnly');
		mxUtils.write(selectionOption, mxResources.get('selectionOnly'));
		typeSelect.appendChild(selectionOption);
	}
	
	if (mxUtils.indexOf(Editor.aiActions, 'assist') >= 0)
	{
		helpOption.setAttribute('value', 'assist');
		mxUtils.write(helpOption, mxResources.get('help'));
		typeSelect.appendChild(helpOption);
	}

	// Adds a drop down for selecting the model from Editor.aiModels
	var modelSelect = typeSelect.cloneNode(false);

	// Lists AI models with valid config and key
	for (var i = 0; i < Editor.aiModels.length; i++)
	{
		var model = Editor.aiModels[i];

		if (Editor.aiConfigs[model.config] && Editor.aiGlobals[
			Editor.aiConfigs[model.config].apiKey] != null)
		{
			var modelOption = document.createElement('option');
			modelOption.setAttribute('value', model.name);
			mxUtils.write(modelOption, model.name);
			modelSelect.appendChild(modelOption);
		}
	}
	
	var publicChat = modelSelect.children.length == 0;
	var inner = document.createElement('div');
	inner.style.whiteSpace = 'nowrap';
	inner.style.textOverflow = 'clip';
	inner.style.cursor = 'default';

	var inp = document.createElement('input');
	inp.setAttribute('type', 'text');
	inp.style.width = '100%';
	inp.style.outline = 'none';
	inp.style.border = 'none';
	inp.style.background = 'transparent';
	inp.style.padding = '6px 30px 6px 10px';
	inp.style.boxSizing = 'border-box';
	inner.appendChild(inp);

	var sendImg = document.createElement('img');
	sendImg.setAttribute('src', Editor.sendImage);
	sendImg.setAttribute('title', mxResources.get('sendMessage'));
	sendImg.className = 'geAdaptiveAsset';
	sendImg.style.position = 'relative';
	sendImg.style.cursor = 'pointer';
	sendImg.style.opacity = '0.5';
	sendImg.style.height = '19px';
	sendImg.style.left = '-28px';
	sendImg.style.top = '5px';
	inner.appendChild(sendImg);
	user.appendChild(inner);

	if (!publicChat)
	{
		if (urlParams['test'] != 1)
		{
			createPublicOption.parentNode.removeChild(createPublicOption);
		}

		options.appendChild(typeSelect);

		if (modelSelect.children.length > 1)
		{
			options.appendChild(modelSelect);
		}
		
		user.appendChild(options);
	}

	if (typeSelect.children.length > 0)
	{
		typeSelect.value = typeSelect.children[0].value;
	}

	var ignoreChange = false;
	var lastType = typeSelect.value;

	var updateDropdowns = function()
	{
		inp.setAttribute('placeholder', mxResources.get(
			(typeSelect.value == 'create' ||
			typeSelect.value == 'createPublic') ?
			'describeYourDiagram' :
			'askMeAnything'));
	};

	updateDropdowns();

	function typeChanged()
	{
		if (!ignoreChange)
		{
			lastType = typeSelect.value;
				updateDropdowns();
		}

		modelSelect.style.display =
			(typeSelect.value == 'createPublic') ?
				'none' : '';
	};

	mxEvent.addListener(typeSelect, 'change', typeChanged);
	typeChanged();

	function updateType()
	{
		ignoreChange = true;
		typeSelect.value = lastType;

		if (graph.isSelectionEmpty())
		{
			selectionOption.setAttribute('disabled', 'disabled');

			if (typeSelect.value == 'selectionOnly')
			{
				typeSelect.value = 'includeCopyOfMyDiagram';
			}
		}
		else
		{
			selectionOption.removeAttribute('disabled');
		}

		if (editorUi.isDiagramEmpty())
		{
			includeOption.setAttribute('disabled', 'disabled');

			if (typeSelect.value == 'includeCopyOfMyDiagram')
			{
				typeSelect.value = 'help';
			}
		}
		else
		{
			includeOption.removeAttribute('disabled');
		}

		ignoreChange = false;
	};

	graph.selectionModel.addListener(mxEvent.CHANGE, updateType);
	graph.getModel().addListener(mxEvent.CHANGE, updateType);
	updateType();

	function createBubble()
	{
		var bubble = document.createElement('div');
		bubble.style.textAlign = 'left';
		bubble.style.padding = '6px';
		bubble.style.margin = '6px 0';

		return bubble;
	}

	function addBubble(text)
	{
		var bubble = createBubble();
		mxUtils.write(bubble, text);
		hist.appendChild(bubble);

		return bubble;
	};

	function addMessage(prompt)
	{
		var elts = [];
		var bubble = addBubble(prompt);
		elts.push(bubble);

		bubble.style.marginBottom = '2px';
		bubble.style.marginLeft = '40%';
		bubble.style.borderRadius = '10px';
		bubble.style.backgroundColor = 'light-dark(#e0e0e0, #3a3a3a)';

		var buttons = document.createElement('div');
		buttons.className = 'geInlineButtons';
		buttons.style.display = 'flex';
		buttons.style.justifyContent = 'end';
		elts.push(buttons);

		var btn = document.createElement('img');
		btn.className = 'geAdaptiveAsset geLibraryButton';
		btn.setAttribute('src', Editor.trashImage);
		btn.setAttribute('title', mxResources.get('remove'));
		buttons.appendChild(btn);

		mxEvent.addListener(btn, 'click', mxUtils.bind(this, function(evt)
		{
			if (mxEvent.isShiftDown(evt))
			{
				hist.innerHTML = '';
			}
			else
			{
				// Removes all elements in elts from their parent
				for (var i = 0; i < elts.length; i++)
				{
					if (elts[i].parentNode != null)
					{
						elts[i].parentNode.removeChild(elts[i]);
					}
				}

				elts = [];
			}
		}));

		btn = btn.cloneNode();
		btn.setAttribute('src', Editor.copyImage);
		btn.setAttribute('title', mxResources.get('copy'));
		mxEvent.addListener(btn, 'click', mxUtils.bind(this, function()
		{
			editorUi.writeTextToClipboard(prompt, mxUtils.bind(this, function(e)
			{
				editorUi.handleError(e);
			}), function()
			{
				editorUi.alert(mxResources.get('copiedToClipboard'));
			});
		}));
		buttons.appendChild(btn);

		btn = btn.cloneNode();
		btn.setAttribute('src', Editor.editImage);
		btn.setAttribute('title', mxResources.get('edit'));
		buttons.appendChild(btn);

		mxEvent.addListener(btn, 'click', mxUtils.bind(this, function()
		{
			inp.value = prompt;
			inp.focus();

			if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
			{
				inp.select();
			}
			else
			{
				document.execCommand('selectAll', false, null);
			}
		}));
		
		hist.appendChild(buttons);
		
		var waiting = addBubble('');
		waiting.className = 'geSidebar';
		waiting.style.marginTop = '2px';

		function createRetryButton(title)
		{
			var buttons = document.createElement('div');
			buttons.style.display = 'flex';

			var btn = document.createElement('img');
			btn.className = 'geAdaptiveAsset geLibraryButton';
			btn.setAttribute('src', Editor.refreshImage);
			btn.setAttribute('title', (title != null) ? title : mxResources.get('tryAgain'));
			buttons.appendChild(btn);
			mxEvent.addListener(btn, 'click', processMessage);
			
			return buttons;
		};

		function parseAIMarkup(text) {
			return mxUtils.htmlEntities(text, false)
				// Headings (consume surrounding newlines)
				.replace(/\n*^##### (.+)$\n*/gm, '<h5>$1</h5>')
				.replace(/\n*^#### (.+)$\n*/gm, '<h4>$1</h4>')
				.replace(/\n*^### (.+)$\n*/gm, '<h3>$1</h3>')
				.replace(/\n*^## (.+)$\n*/gm, '<h2>$1</h2>')
				.replace(/\n*^# (.+)$\n*/gm, '<h1>$1</h1>')
				// Bold
				.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
				// Italic
				.replace(/\*(.+?)\*/g, '<em>$1</em>')
				// Inline code
				.replace(/`([^`]+)`/g, '<code>$1</code>')
		};
		
		function createDivForText(text)
		{
			var wrapper = document.createElement('div');
			wrapper.style.whiteSpace = 'pre-wrap';
			wrapper.style.overflow = 'auto';
			wrapper.innerHTML = Graph.sanitizeHtml(parseAIMarkup(text));

			return wrapper;
		};
		
		function createError(message)
		{
			var title = mxResources.get('error') + ': ';
			var wrapper = document.createElement('div');
			wrapper.style.whiteSpace = 'pre-wrap';

			if (message.substring(0, title.length) != title)
			{
				message = title + message;
			}

			mxUtils.write(wrapper, message);
			wrapper.appendChild(createRetryButton());
			
			return wrapper;
		};

		var handleError = mxUtils.bind(this, function(e)
		{
			waiting.innerHTML = '';
			waiting.appendChild(createError(e.message));
			waiting.scrollIntoView({behavior: 'smooth',
				block: 'end', inline: 'nearest'});
			EditorUi.debug('EditorUi.ChatWindow.handleError',
				'error', e);
			
			if (window.console != null)
			{
				console.error(e);
			}
		});

		var page = editorUi.currentPage;
		var theModel = modelSelect.value;
		var type = typeSelect.value;
		var aiModel = null;

		for (var i = 0; i < Editor.aiModels.length; i++)
		{
			var model = Editor.aiModels[i];

			if (model.name == theModel)
			{
				aiModel = model;
				break;
			}
		}

		if (type != 'createPublic' && (aiModel == null ||
			Editor.aiConfigs[aiModel.config] == null))
		{
			handleError({message: mxResources.get('invalidCallFnNotFound', [theModel])});

			return;
		}
		
		var config = (aiModel != null) ? Editor.aiConfigs[aiModel.config] : null;
		var thePrompt = prompt;
		var sentModel = null;	
		var t0 = Date.now();
		var data = null;
		var xml = null;

		if (type == 'includeCopyOfMyDiagram' || type == 'selectionOnly')
		{
			var enc = new mxCodec(mxUtils.createXmlDocument());
			
			// Ignores unselected cells
			if (type == 'selectionOnly')
			{
				enc.isObjectIgnored = function(obj)
				{
					return obj.constructor == mxCell &&
						(!graph.model.isRoot(obj) &&
						!graph.model.isLayer(obj) &&
						!graph.isCellSelected(obj) &&
						!graph.isAncestorSelected(obj));
				};
			}

			xml = enc.encode(graph.getModel());

			// Sets xml.ownerDocument.documentElement == xml so
			// that forward references work correctly
			xml.ownerDocument.appendChild(xml);
			data = mxUtils.getXml(xml);
		}

		var resolver = function(name)
		{
			var value = null;

			if (name == 'prompt')
			{
				value = thePrompt;
			}
			else if (name == 'data' && xml != null)
			{
				value = data;
			}
			else if (name == 'model')
			{
				value = aiModel.model;
			}
			else if (name == 'apiKey')
			{
				name = config.apiKey;
			}
			else if (name == 'action')
			{
				if (type == 'selectionOnly' || type == 'includeCopyOfMyDiagram')
				{
					name = 'update';
				}
				else
				{
					name = type;
				}
			}

			if (value == null)
			{
				value = Editor.replacePlaceholders(Editor.aiGlobals[name], resolver);
			}

			return value;
		};

		// Clones all properties of the given object and replaces
		// placeholders in string properties recursively
		var populateTemplate = function(obj, result)
		{
			if (result == null)
			{
				result = new obj.constructor();
			}

			for (var key in obj)
			{
				var value = obj[key];

				if (typeof value === 'object')
				{
					result[key] = populateTemplate(value);
				}
				else if (typeof value === 'string')
				{
					result[key] = Editor.replacePlaceholders(value, resolver);
				}
				else 
				{
					result[key] = value;
				}
			}

			return result;
		};

		var params = (config != null) ? populateTemplate(config.request) : null;

		var processMessage = mxUtils.bind(this, function()
		{
			waiting.innerHTML = '';
			elts.push(waiting);

			var wrapper = document.createElement('div');
			wrapper.style.display = 'flex';
			wrapper.style.alignItems = 'center';
			mxUtils.write(wrapper, mxResources.get('loading') + '...');

			var img = document.createElement('img');
			img.className = 'geAdaptiveAsset';
			img.setAttribute('src', Editor.svgSpinImage);
			img.style.width = '16px';
			img.style.height = '16px';
			img.style.marginLeft = '6px';
			wrapper.appendChild(img);
			waiting.appendChild(wrapper);

			waiting.scrollIntoView({ behavior: 'smooth',
				block: 'end', inline: 'nearest'});
			
			var handleResponse = mxUtils.bind(this, function(data, prompt)
			{
				var dt = Date.now() - t0;
				EditorUi.debug('EditorUi.ChatWindow.handleResponse',
					'data', data, 'prompt', [prompt], 'time', dt);
				var cells = null;

				if (data != null && data.length > 1 && data[1].length > 0)
				{
					try
					{
						cells = editorUi.stringToCells(data[1]);
					}
					catch (e)
					{
						throw new Error(e.toString() + '\n\n' + data[1]);
					}
				}
				
				if (cells != null && cells.length > 0)
				{
					var bbox = graph.getBoundingBoxFromGeometry(cells);
					editorUi.sidebar.graph.moveCells(cells, -bbox.x, -bbox.y);

					var clickFn = mxUtils.bind(this, function(e)
					{
						if (editorUi.sidebar != null)
						{
							editorUi.sidebar.hideTooltip();
						}

						if (xml != null && sentModel == null)
						{
							var dec = new mxCodec(xml.ownerDocument);
							sentModel = new mxGraphModel();
							dec.decode(xml, sentModel);
						}

						graph.model.beginUpdate();
						try
						{
							if (sentModel != null && page != null &&
								editorUi.getPageIndex(page) != null)
							{
								editorUi.selectPage(page);
								var doc = mxUtils.parseXml(data[1]);
								var codec = new mxCodec(doc);
								var receivedModel = new mxGraphModel();
								codec.decode(doc.documentElement, receivedModel);

								// Creates a diff of the sent and recevied diagram
								// to patch the current page and not lose changes
								var patch = editorUi.diffCells(
									sentModel.root, receivedModel.root);
								editorUi.patchPage(page, patch, null, true);
								EditorUi.debug('EditorUi.ChatWindow.handleResponse',
									'sentModel', sentModel, 'receivedModel', receivedModel,
									'patch', patch);
							}
							else
							{
								var pt = graph.getFreeInsertPoint();
								graph.setSelectionCells(graph.importCells(
									cells, pt.x, pt.y));
								EditorUi.debug('EditorUi.ChatWindow.handleResponse',
									'cells', graph.getSelectionCell());
							}
						}
						finally
						{
							graph.model.endUpdate();
						}

						graph.scrollCellToVisible(graph.getSelectionCell());
						mxEvent.consume(e);
					});

					waiting.innerHTML = '';
					bubble = waiting;

					if (data[0].length > 0)
					{
						bubble.appendChild(createDivForText(data[0]));
					}

					if (data[1].length > 0)
					{
						var svg = editorUi.getSvgForXml(data[1]);
						svg.style.overflow = 'visible';
						svg.style.padding = '1px';
						svg.style.cursor = 'move';
						svg.style.width = '160px';
						svg.style.height = 'auto';
						svg.style.maxHeight = '460px';

						var item = document.createElement('a');
						item.className = 'geItem';
						item.style.padding = '4px';
						item.style.borderRadius = '10px';
						item.appendChild(svg);
						bubble.appendChild(item);
						editorUi.sidebar.createItem(cells, prompt, true, true, bbox.width, bbox.height,
							true, true, clickFn, null, null, null, null, null, item);

						if (!publicChat && type != 'createPublic' && urlParams['test'] == 1)
						{
							item.setAttribute('title', theModel + ' (' + dt + ' ms)');
						}
						
						var buttons = document.createElement('div');
						buttons.style.display = 'flex';

						var btn = document.createElement('img');
						btn.className = 'geAdaptiveAsset geLibraryButton';
						btn.setAttribute('src', Editor.refreshImage);
						btn.setAttribute('title', mxResources.get('refresh'));
						buttons.appendChild(btn);
						mxEvent.addListener(btn, 'click', processMessage);

						if (editorUi.getServiceName() == 'draw.io')
						{
							btn = btn.cloneNode();
							btn.setAttribute('src', Editor.shareImage);
							btn.setAttribute('title', mxResources.get(!editorUi.isStandaloneApp() ?
								'openInNewWindow' : 'export'));
							buttons.appendChild(btn);

							mxEvent.addListener(btn, 'click', mxUtils.bind(this, function(evt)
							{
								if (!editorUi.isStandaloneApp())
								{
									editorUi.editor.editAsNew(data[1]);
								}
								else
								{
									editorUi.saveData('export.xml', 'xml', data[1], 'text/xml');
								}
							}));
						}

						btn = btn.cloneNode();
						btn.setAttribute('src', Editor.magnifyImage);
						btn.setAttribute('title', mxResources.get('preview'));
						buttons.appendChild(btn);

						mxEvent.addListener(btn, 'click', mxUtils.bind(this, function(evt)
						{
							var ww = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
							var wh = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
							
							editorUi.sidebar.createTooltip(bubble, cells, Math.min(ww - 120, 1600), Math.min(wh - 120, 1200),
								prompt, true, new mxPoint(mxEvent.getClientX(evt), mxEvent.getClientY(evt)), true, function()
								{
									wasVisible = editorUi.sidebar.tooltip != null &&
										editorUi.sidebar.tooltip.style.display != 'none';
								}, true, false);
						}));

						btn = btn.cloneNode();

						if (xml != null && page != null &&
							editorUi.getPageIndex(page) != null)
						{
							btn.setAttribute('src', Editor.checkImage);
							btn.setAttribute('title', mxResources.get('apply'));
						}
						else
						{
							btn.setAttribute('src', Editor.plusImage);
							btn.setAttribute('title', mxResources.get('insert'));
						}

						buttons.appendChild(btn);
						mxEvent.addListener(btn, 'click', clickFn);
						bubble.appendChild(buttons);
					}

					if (data[2].length > 0)
					{
						bubble.appendChild(createDivForText(data[2]));
					}
				}
				else
				{
					waiting.innerHTML = '';
					bubble = waiting;
					waiting.scrollIntoView({behavior: 'smooth',
						block: 'end', inline: 'nearest'});

					if (data == null)
					{
						mxUtils.write(bubble, mxResources.get('errShowingDiag'));
					}
					else
					{
						bubble.style.whiteSpace = 'pre-wrap';
						bubble.appendChild(createDivForText(data[0]));
						bubble.appendChild(createDivForText(data[2]));
						bubble.appendChild(createRetryButton(mxResources.get('refresh')));
					}
				}

				bubble.scrollIntoView({behavior: 'smooth',
					block: 'end', inline: 'nearest'});
			});

			if (publicChat || type == 'createPublic')
			{
				editorUi.generateOpenAiMermaidDiagram(thePrompt, function(xml)
				{
					handleResponse(['', xml, ''], thePrompt);
				}, handleError, true);
			}
			else
			{
				editorUi.createTimeout(editorUi.editor.generateTimeout, mxUtils.bind(this, function(timeout)
				{
					var handleErrorWithTimeout = mxUtils.bind(this, function(e)
					{
						timeout.clear();
						handleError(e);
					});

					var url = Editor.replacePlaceholders(config.endpoint, resolver);
					var req = new mxXmlRequest(url, JSON.stringify(params), 'POST');
					
					req.setRequestHeaders = mxUtils.bind(this, function(request, params)
					{
						request.setRequestHeader('Content-Type', 'application/json');

						for (var key in config.requestHeaders)
						{
							request.setRequestHeader(key, Editor.replacePlaceholders(
								config.requestHeaders[key], resolver));
						}
					});

					EditorUi.debug('EditorUi.ChatWindow.addMessage send', 'url', url,
						'params', params, 'aiModel', aiModel, 'config', config);

					req.send(mxUtils.bind(this, function(req)
					{
						if (timeout.clear())
						{
							try
							{
								if (req.getStatus() >= 200 && req.getStatus() <= 299)
								{
									var response = JSON.parse(req.getText());
									var result = Editor.executeSimpleJsonPath(response, config.responsePath);
									var text = mxUtils.trim((result.length > 0) ? result[0] : req.getText());
									var mermaid = editorUi.extractMermaidDeclaration(text);
									EditorUi.debug('EditorUi.ChatWindow.addMessage response',
										'params', params, 'response', response,
										'text', [text], 'mermaid', [mermaid]);

									if (mermaid == null)
									{
										handleResponse(Editor.extractGraphModelFromText(text), thePrompt);
									}
									else
									{
										editorUi.parseMermaidDiagram(mermaid, null, mxUtils.bind(this, function(xml)
										{
											handleResponse(['', xml, ''], thePrompt);
										}), mxUtils.bind(this, function(e)
										{
											handleErrorWithTimeout(e);
										}), null, true);
									}
								}
								else
								{
									var result = 'Error: ' + req.getStatus();

									try
									{
										var resp = JSON.parse(req.getText());

										if (resp != null && resp.error != null &&
											resp.error.message != null)
										{
											result = resp.error.message;
										}
									}
									catch (e)
									{
										// ignore
									}
									
									waiting.innerHTML = '';
									mxUtils.write(waiting, result);
									waiting.scrollIntoView(
										{behavior: 'smooth', block: 'end',
										inline: 'nearest'});
								}
							}
							catch (e)
							{
								handleErrorWithTimeout(e);
							}
						}
					}), handleErrorWithTimeout);
				}), function(e)
				{
					waiting.innerHTML = '';
					waiting.appendChild(createError(e.message));
					waiting.scrollIntoView({behavior: 'smooth',
						block: 'end', inline: 'nearest'});
					EditorUi.debug('EditorUi.ChatWindow.addMessage',
						'error', e);
				});
			}
		});

		processMessage();
	};

	div.appendChild(user);

	function send()
	{
		if (mxUtils.trim(inp.value) != '')
		{
			try
			{	
				addMessage(inp.value);
				inp.value = '';
			}
			catch (e)
			{
				EditorUi.debug('EditorUi.ChatWindow.send', 'error', e);
			}
		}
	};

	mxEvent.addListener(sendImg, 'click', send);

	mxEvent.addListener(inp, 'keydown', function(evt)
	{
		if (evt.keyCode == 13 && !mxEvent.isShiftDown(evt))
		{
			send();
		}
	});

	this.generate = mxUtils.bind(this, function(prompt)
	{
		inp.value = prompt;
		send();
	});

	this.window = new mxWindow(mxResources.get('generate'),
		div, x, y, w, h, true, true);
	this.window.minimumSize = new mxRectangle(0, 0, 120, 100);
	this.window.destroyOnClose = false;
	this.window.setMaximizable(false);
	this.window.setResizable(true);
	this.window.setClosable(true);

	// Adds help icon to title bar
	if (!editorUi.isOffline())
	{
		var icon = editorUi.createHelpIcon('https://www.drawio.com/doc/faq/configure-ai-options');
		icon.style.cursor = 'help';
		icon.style.opacity = '0.5';
		this.window.buttons.insertBefore(icon, this.window.buttons.firstChild);
	}

	this.window.addListener(mxEvent.DESTROY, mxUtils.bind(this, function()
	{
		graph.getModel().removeListener(updateType);
	}));

	this.window.addListener('show', mxUtils.bind(this, function()
	{
		this.window.fit();
		inp.focus();
	}));

	editorUi.installResizeHandler(this, true);
};

/**
 * 
 */
var TagsWindow = function(editorUi, x, y, w, h)
{
	var graph = editorUi.editor.graph;
	var helpButton = null;

	if (!editorUi.isOffline() || mxClient.IS_CHROMEAPP)
	{
		helpButton = editorUi.menus.createHelpLink('https://www.drawio.com/blog/tags-in-diagrams');
	}

	var tagsComponent = editorUi.editor.graph.createTagsDialog(mxUtils.bind(this, function()
	{
		return this.window.isVisible();
	}), null, function(allTags, updateFn)
	{
		if (graph.isEnabled())
		{
			var dlg = new FilenameDialog(editorUi, '', mxResources.get('add'), function(newValue)
			{
				editorUi.hideDialog();
				
				if (newValue != null && newValue.length > 0)
				{
					var temp = newValue.split(' ');
					var newTags = [];
					var tags = [];

					for (var i = 0; i < temp.length; i++)
					{
						var token = mxUtils.trim(temp[i]);

						if (token != '')
						{
							tags.push(token);

							if (mxUtils.indexOf(allTags, token) < 0)
							{
								newTags.push(token);
							}
						}
					}

					if (graph.isSelectionEmpty())
					{
						updateFn(allTags.concat(newTags));
					}
					else
					{
						graph.addTagsForCells(graph.getSelectionCells(), tags);
					}
				}
			}, mxResources.get('tags'), null, null, 'https://www.drawio.com/blog/tags-in-diagrams');
			
			editorUi.showDialog(dlg.container, 320, 80, true, true);
			dlg.init();
		}
	}, helpButton);

	var div = tagsComponent.div;
	this.window = new mxWindow(mxResources.get('tags'), div, x, y, w, h, true, true);
	this.window.minimumSize = new mxRectangle(0, 0, 212, 120);
	this.window.destroyOnClose = false;
	this.window.setMaximizable(false);
	this.window.setResizable(true);
	this.window.setClosable(true);

	this.window.addListener('show', mxUtils.bind(this, function()
	{
		tagsComponent.refresh();
		this.window.fit();
	}));
	
	editorUi.installResizeHandler(this, true);
};

/**
 * Constructs a new auth dialog.
 */
var AuthDialog = function(editorUi, peer, showRememberOption, fn)
{
	var div = document.createElement('div');
	div.style.textAlign = 'center';
	
	var hd = document.createElement('p');
	hd.style.fontSize = '16pt';
	hd.style.padding = '0px';
	hd.style.margin = '0px';
	hd.style.color = 'gray';
	
	mxUtils.write(hd, mxResources.get('authorizationRequired'));
	
	var service = 'Unknown';
	
	var img = document.createElement('img');
	img.setAttribute('border', '0');
	img.setAttribute('align', 'absmiddle');
	img.style.marginRight = '10px';

	if (peer == editorUi.drive)
	{
		service = mxResources.get('googleDrive');
		img.src = IMAGE_PATH + '/google-drive-logo-white.svg';
	}
	else if (peer == editorUi.dropbox)
	{
		service = mxResources.get('dropbox');
		img.src = IMAGE_PATH + '/dropbox-logo-white.svg';
	}
	else if (peer == editorUi.oneDrive)
	{
		service = mxResources.get('oneDrive');
		img.src = IMAGE_PATH + '/onedrive-logo-white.svg';
	}
	else if (peer == editorUi.gitHub)
	{
		service = mxResources.get('github');
		img.src = IMAGE_PATH + '/github-logo-white.svg';
	}
	else if (peer == editorUi.gitLab)
	{
		service = mxResources.get('gitlab');
		img.src = IMAGE_PATH + '/gitlab-logo.svg';
		img.style.width = '32px';
	}
	else if (peer == editorUi.trello)
	{
		service = mxResources.get('trello');
		img.src = IMAGE_PATH + '/trello-logo-white.svg';
	}
	else if (peer == editorUi.m365)
	{
		service = mxResources.get('m365');
		img.src = IMAGE_PATH + '/onedrive-logo-white.svg';
	}
	
	var p = document.createElement('p');
	mxUtils.write(p, mxResources.get('authorizeThisAppIn', [service]));

	var cb = document.createElement('input');
	cb.setAttribute('type', 'checkbox');
	
	var button = mxUtils.button(mxResources.get('authorize'), function()
	{
		fn(cb.checked);
	});

	button.insertBefore(img, button.firstChild);
	button.style.marginTop = '6px';
	button.className = 'geBigButton';
	button.style.fontSize = '18px';
	button.style.padding = '14px';

	div.appendChild(hd);
	div.appendChild(p);
	div.appendChild(button);
	
	if (showRememberOption)
	{
		var p2 = document.createElement('p');
		p2.style.marginTop = '20px';
		p2.appendChild(cb);
		var span = document.createElement('span');
		mxUtils.write(span, ' ' + mxResources.get('rememberMe'));
		p2.appendChild(span);
		div.appendChild(p2);
		cb.checked = true;
		cb.defaultChecked = true;
		
		mxEvent.addListener(span, 'click', function(evt)
		{
			cb.checked = !cb.checked;
			mxEvent.consume(evt);
		});
	}
	
	this.container = div;
};

var MoreShapesDialog = function(editorUi, expanded, entries) 
{
	entries = (entries != null) ? entries : editorUi.sidebar.entries;
	var div = document.createElement('div');
	var newEntries = [];
	
	// Adds custom sections first
	if (editorUi.sidebar.customEntries != null)
	{
		for (var i = 0; i < editorUi.sidebar.customEntries.length; i++)
		{
			var section = editorUi.sidebar.customEntries[i] || {};
			var tmp = {title: editorUi.getResource(section.title), entries: []};
			
			for (var j = 0; section.entries != null && j < section.entries.length; j++)
			{
				var entry = section.entries[j];
				tmp.entries.push({id: entry.id, title:
					editorUi.getResource(entry.title),
					desc: editorUi.getResource(entry.desc),
					image: entry.preview});
			}
			
			if (tmp.entries.length > 0)
			{
				newEntries.push(tmp);
			}
		}
	}
	
	// Adds built-in sections and filter entries
	for (var i = 0; i < entries.length; i++)
	{
		if (editorUi.sidebar.enabledLibraries == null)
		{
			newEntries.push(entries[i]);
		}
		else
		{
			var tmp = {title: entries[i].title, entries: []};
			
			for (var j = 0; j < entries[i].entries.length; j++)
			{
				if (mxUtils.indexOf(editorUi.sidebar.enabledLibraries,
					entries[i].entries[j].id) >= 0)
				{
					tmp.entries.push(entries[i].entries[j]);
				}
			}
			
			if (tmp.entries.length > 0)
			{
				newEntries.push(tmp);
			}
		}
	}
	
	entries = newEntries;
	
	if (expanded)
	{
		var addEntries = mxUtils.bind(this, function(e)
		{
			for (var i = 0; i < e.length; i++)
			{
				(function(section)
				{
					var title = listEntry.cloneNode(false);
					title.style.fontWeight = 'bold';
					title.style.backgroundColor = 'light-dark(#e5e5e5, #505759)';
					title.style.padding = '6px 0px 6px 20px';
					mxUtils.write(title, section.title);
					list.appendChild(title);
		
					for (var j = 0; j < section.entries.length; j++)
					{
						(function(entry)
						{
							var option = listEntry.cloneNode(false);
							option.style.cursor = 'pointer';
							option.style.padding = '4px 0px 4px 20px';
							option.style.whiteSpace = 'nowrap';
							option.style.overflow = 'hidden';
							option.style.textOverflow = 'ellipsis';
							option.setAttribute('title', entry.title + ' (' + entry.id + ')');
							
							var checkbox = document.createElement('input');
							checkbox.setAttribute('type', 'checkbox');
							checkbox.checked = editorUi.sidebar.isEntryVisible(entry.id);
							checkbox.defaultChecked = checkbox.checked;
							option.appendChild(checkbox);
							mxUtils.write(option, ' ' + entry.title);
		
							list.appendChild(option);
							
							var itemClicked = function(evt)
							{
								if (evt == null || mxEvent.getSource(evt).nodeName != 'INPUT')
								{
									preview.style.textAlign = 'center';
									preview.style.padding = '0px';
									preview.style.color = '';
									preview.innerText = '';
									
									if (entry.desc != null)
									{
										var pre = document.createElement('pre');
										pre.style.boxSizing = 'border-box';
										pre.style.fontFamily = 'inherit';
										pre.style.margin = '20px';
										pre.style.right = '0px';
										pre.style.textAlign = 'left';
										mxUtils.write(pre, entry.desc);
										preview.appendChild(pre);
									}
									
									if (entry.imageCallback != null)
									{
										entry.imageCallback(preview);
									}
									else if (entry.image != null)
									{
										var img = document.createElement('img');
										img.setAttribute('border', '0');
										img.style.maxWidth = '100%';
										img.setAttribute('src', entry.image);
										preview.appendChild(img);
									}
									else if (entry.desc == null)
									{
										preview.style.padding = '20px';
										preview.style.color = 'rgb(179, 179, 179)';
										mxUtils.write(preview, mxResources.get('noPreview'));
									}
									
									if (currentListItem != null)
									{
										currentListItem.style.backgroundColor = '';
									}
									
									currentListItem = option;
									currentListItem.style.backgroundColor = 'light-dark(#ebf2f9, #000000)';
									
									if (evt != null)
									{
										mxEvent.consume(evt);
									}
								}
							};
							
							mxEvent.addListener(option, 'click', itemClicked);
							mxEvent.addListener(option, 'dblclick', function(evt)
							{
								checkbox.checked = !checkbox.checked;
								mxEvent.consume(evt);
							});
							
							applyFunctions.push(function()
							{
								return (checkbox.checked) ? entry.id : null;
							});
							
							// Selects first entry
							if (i == 0 && j == 0)
							{
								itemClicked();
							}
						})(section.entries[j]);
					}
				})(e[i]);
			}
		});
		
		var hd = document.createElement('div');
		hd.className = 'geDialogTitle';
		mxUtils.write(hd, mxResources.get('shapes'));
		hd.style.position = 'absolute';
		hd.style.top = '0px';
		hd.style.left = '0px';
		hd.style.lineHeight = '40px';
		hd.style.height = '40px';
		hd.style.right = '0px';
		
		var list = document.createElement('div');
		var preview = document.createElement('div');
		
		list.style.position = 'absolute';
		list.style.top = '40px';
		list.style.left = '0px';
		list.style.width = '202px';
		list.style.bottom = '60px';
		list.style.overflow = 'auto';
		
		preview.style.position = 'absolute';
		preview.style.left = '202px';
		preview.style.right = '0px';
		preview.style.top = '40px';
		preview.style.bottom = '60px';
		preview.style.overflow = 'auto';
		preview.style.borderLeftStyle = 'solid';
		preview.style.borderLeftWidth = '1px';
		preview.style.textAlign = 'center';
		
		var currentListItem = null;
		var applyFunctions = [];
		
		var listEntry = document.createElement('div');
		listEntry.style.position = 'relative';
		listEntry.style.left = '0px';
		listEntry.style.right = '0px';
		
		addEntries(entries);
		div.style.padding = '30px';
		
		div.appendChild(hd);
		div.appendChild(list);
		div.appendChild(preview);
		
		var buttons = document.createElement('div');
		buttons.className = 'geDialogFooter';
		buttons.style.position = 'absolute';
		buttons.style.paddingRight = '16px';
		buttons.style.left = '0px';
		buttons.style.right = '0px';
		buttons.style.bottom = '0px';
		buttons.style.height = '60px';
		buttons.style.lineHeight = '52px';
		
		var labels = document.createElement('input');
		labels.setAttribute('type', 'checkbox');
		labels.style.position = 'relative';
		labels.style.top = '1px';
		labels.checked = editorUi.sidebar.sidebarTitles;
		labels.defaultChecked = labels.checked;
		buttons.appendChild(labels);
		var span = document.createElement('span');
		mxUtils.write(span, ' ' + mxResources.get('labels'));
		span.style.paddingRight = '20px';
		buttons.appendChild(span);
		
		mxEvent.addListener(span, 'click', function(evt)
		{
			labels.checked = !labels.checked;
			mxEvent.consume(evt);
		});

		var cb = document.createElement('input');
		cb.setAttribute('type', 'checkbox');
		
		if (isLocalStorage || mxClient.IS_CHROMEAPP)
		{
			var span = document.createElement('span');
			span.style.paddingRight = '20px';
			span.appendChild(cb);
			mxUtils.write(span, ' ' + mxResources.get('rememberThisSetting'));
			cb.style.position = 'relative';
			cb.style.top = '1px';
			cb.checked = true;
			cb.defaultChecked = true;
			
			mxEvent.addListener(span, 'click', function(evt)
			{
				if (mxEvent.getSource(evt) != cb)
				{
					cb.checked = !cb.checked;
					mxEvent.consume(evt);
				}
			});
			
			buttons.appendChild(span);
		}
		
		var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
		{
			editorUi.hideDialog();
		});
		cancelBtn.className = 'geBtn';
		
		var applyBtn = mxUtils.button(mxResources.get('apply'), function()
		{
	    	editorUi.hideDialog();
	    	var libs = [];
			
			for (var i = 0; i < applyFunctions.length; i++)
			{
				var lib = applyFunctions[i].apply(this, arguments);
				
				if (lib != null)
				{
					libs.push(lib);
				}
			}

			// Redirects scratchpad and search entries
			if ((Editor.currentTheme == 'simple' ||
				Editor.currentTheme == 'sketch' ||
				Editor.currentTheme == 'min') &&
				Editor.isSettingsEnabled())
			{
				var idx = mxUtils.indexOf(libs, '.scratchpad');

				if ((editorUi.scratchpad != null) != (idx >= 0 && libs.splice(idx, 1).length > 0))
				{
					editorUi.toggleScratchpad();
				}

				// Handles search after scratchpad
				idx = mxUtils.indexOf(libs, 'search');
				mxSettings.settings.search = (idx >= 0 && libs.splice(idx, 1).length > 0);
				editorUi.sidebar.showPalette('search', mxSettings.settings.search);

				if (cb.checked)
				{
					mxSettings.save();
				}
			}
			
			editorUi.sidebar.showEntries(libs.join(';'), cb.checked, true);
			editorUi.setSidebarTitles(labels.checked, cb.checked);
		});
		applyBtn.className = 'geBtn gePrimaryBtn';
		
		if (editorUi.editor.cancelFirst)
		{
			buttons.appendChild(cancelBtn);
			buttons.appendChild(applyBtn);
		}
		else
		{
			buttons.appendChild(applyBtn);
			buttons.appendChild(cancelBtn);
		}
		
		div.appendChild(buttons);
	}
	else
	{
		var libFS = document.createElement('table');
		var tbody = document.createElement('tbody');
		div.style.height = '100%';
		div.style.overflow = 'auto';
		var row = document.createElement('tr');
		libFS.style.width = '100%';
		
		var leftDiv = document.createElement('td');
		var midDiv = document.createElement('td');
		var rightDiv = document.createElement('td');
				
		var addLibCB = mxUtils.bind(this, function(wrapperDiv, title, key) 
		{
			var libCB = document.createElement('input');
			libCB.type = 'checkbox';
			libFS.appendChild(libCB);
			
			libCB.checked = editorUi.sidebar.isEntryVisible(key);
			
			var libSpan = document.createElement('span');
			mxUtils.write(libSpan, title);
			
			var label = document.createElement('div');
			label.style.display = 'block';
			label.appendChild(libCB);
			label.appendChild(libSpan);
			
			mxEvent.addListener(libSpan, 'click', function(evt)
			{
				libCB.checked = !libCB.checked;
				mxEvent.consume(evt);
			});
			
			wrapperDiv.appendChild(label);
			
			return function()
			{
				return (libCB.checked) ? key : null;
			};
		});
		
		row.appendChild(leftDiv);
		row.appendChild(midDiv);
		row.appendChild(rightDiv);
	
		tbody.appendChild(row);
		libFS.appendChild(tbody);
		
		var applyFunctions = [];
		var count = 0;
		
		// Counts total number of entries
		for (var i = 0; i < entries.length; i++)
		{
			for (var j = 0; j < entries[i].entries.length; j++)
			{
				count++;
			}
		}
		
		// Distributes entries on columns
		var cols = [leftDiv, midDiv, rightDiv];
		var counter = 0;
		
		for (var i = 0; i < entries.length; i++)
		{
			(function(section)
			{
				for (var j = 0; j < section.entries.length; j++)
				{
					(function(entry)
					{
						var index = Math.floor(counter / (count / 3));
						applyFunctions.push(addLibCB(cols[index], entry.title, entry.id));
						counter++;
					})(section.entries[j]);
				}
			})(entries[i]);
		}

		div.appendChild(libFS);

		var remember = document.createElement('div');
		remember.style.marginTop = '18px';
		remember.style.textAlign = 'center';

		var cb = document.createElement('input');
		
		if (isLocalStorage)
		{
			cb.setAttribute('type', 'checkbox');
			cb.checked = true;
			cb.defaultChecked = true;
			remember.appendChild(cb);
			var span = document.createElement('span');
			mxUtils.write(span, ' ' + mxResources.get('rememberThisSetting'));
			remember.appendChild(span);
			
			mxEvent.addListener(span, 'click', function(evt)
			{
				cb.checked = !cb.checked;
				mxEvent.consume(evt);
			});
		}
		
		div.appendChild(remember);
		
		var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
		{
			editorUi.hideDialog();
		});
		cancelBtn.className = 'geBtn';
		
		var applyBtn = mxUtils.button(mxResources.get('apply'), function()
		{
			var libs = ['search'];
			
			for (var i = 0; i < applyFunctions.length; i++)
			{
				var lib = applyFunctions[i].apply(this, arguments);
				
				if (lib != null)
				{
					libs.push(lib);
				}
			}
			
			editorUi.sidebar.showEntries((libs.length > 0) ? libs.join(';') : '', cb.checked);
	    	editorUi.hideDialog();
		});
		applyBtn.className = 'geBtn gePrimaryBtn';
		
		var buttons = document.createElement('div');
		buttons.style.marginTop = '26px';
		buttons.style.textAlign = 'right';
		
		if (editorUi.editor.cancelFirst)
		{
			buttons.appendChild(cancelBtn);
			buttons.appendChild(applyBtn);
		}
		else
		{
			buttons.appendChild(applyBtn);
			buttons.appendChild(cancelBtn);
		}
	
		div.appendChild(buttons);
	}

	this.container = div;
};

var PluginsDialog = function(editorUi, addFn, delFn, closeOnly) 
{
	var div = document.createElement('div');
	var inner = document.createElement('div');
	
	inner.style.height = '180px';
	inner.style.overflow = 'auto';

	var plugins = mxSettings.getPlugins().slice();
	var changed = false;
	
	function refresh()
	{
		changed = true;

		if (plugins.length == 0)
		{
			inner.innerText = mxResources.get('noPlugins');
		}
		else
		{
			inner.innerText = '';
			
			for (var i = 0; i < plugins.length; i++)
			{
				var span = document.createElement('span');
				span.style.display = 'flex';
				span.style.alignItems = 'center';
				span.style.whiteSpace = 'nowrap';

				var img = document.createElement('img');
				img.src = Editor.trashImage;
				img.style.position = 'relative';
				img.style.cursor = 'pointer';
				img.style.marginRight = '4px';
				img.style.display = 'inline-block';
				img.style.width = '18px';
				img.setAttribute('title', mxResources.get('delete'));
				span.appendChild(img);
				
				mxUtils.write(span, plugins[i]);
				inner.appendChild(span);
				
				mxUtils.br(inner);
				
				mxEvent.addListener(img, 'click', (function(index)
				{
					return function()
					{
						editorUi.confirm(mxResources.get('delete') + ' "' + plugins[index] + '"?', function()
						{
							if (delFn != null) 
							{
								delFn(plugins[index]);
							}
							
							plugins.splice(index, 1);
							refresh();
						});
					};
				})(i));
			}
		}
	}
	
	div.appendChild(inner);
	refresh();
	changed = false;

	var addBtn = mxUtils.button(mxResources.get('add'), addFn != null? function()
	{
		addFn(function(newPlugin)
		{
			if (newPlugin && mxUtils.indexOf(plugins, newPlugin) < 0)
			{
				plugins.push(newPlugin);
			}
			
			refresh();
		});
	}
	: function()
	{
		var div = document.createElement('div');
		
		var title = document.createElement('span');
		title.style.marginTop = '6px';
		mxUtils.write(title, mxResources.get('builtinPlugins') + ': ');
		div.appendChild(title);
		
		var pluginsSelect = document.createElement('select');
		pluginsSelect.style.width = '150px';
		
		for (var i = 0; i < App.publicPlugin.length; i++)
		{
			var option = document.createElement('option');
			mxUtils.write(option, App.publicPlugin[i]);
			option.value = App.publicPlugin[i];
			pluginsSelect.appendChild(option);
		}
		
		div.appendChild(pluginsSelect);
		mxUtils.br(div);
		mxUtils.br(div);
		
		var customBtn = mxUtils.button(mxResources.get('custom') + '...', function()
		{
			var dlg = new FilenameDialog(editorUi, '', mxResources.get('add'), function(newValue)
			{
				editorUi.hideDialog();
				
				if (newValue != null && newValue.length > 0)
				{
					var tokens = newValue.split(';');
					
					for (var i = 0; i < tokens.length; i++)
					{
						var token = tokens[i];
						var url = App.pluginRegistry[token];
						
						if (url != null)
						{
							token = url;
						}
						
						if (token.length > 0 && mxUtils.indexOf(plugins, token) < 0)
						{
							plugins.push(token);
						}
					}
					
					refresh();
				}
			}, mxResources.get('enterValue') + ' (' + mxResources.get('url') + ')');
			
			editorUi.showDialog(dlg.container, 300, 80, true, true);
			dlg.init();
		});
		
		customBtn.className = 'geBtn';

		if (!ALLOW_CUSTOM_PLUGINS)
		{
			customBtn.style.display = 'none';
		}
				
		var dlg = new CustomDialog(editorUi, div, mxUtils.bind(this, function()
		{
			var token = App.pluginRegistry[pluginsSelect.value];
			
			if (mxUtils.indexOf(plugins, token) < 0)
			{
				plugins.push(token);
				refresh();
			}
		}), null, null, null, customBtn);
		editorUi.showDialog(dlg.container, 360, 100, true, true);
	});
	
	addBtn.className = 'geBtn';
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	
	cancelBtn.className = 'geBtn';
	
	var applyBtn = mxUtils.button(closeOnly? mxResources.get('close') : mxResources.get('apply'), function()
	{
		if (changed)
		{
			mxSettings.setPlugins(plugins);
			mxSettings.save();
			editorUi.hideDialog();
			editorUi.alert(mxResources.get('restartForChangeRequired'));
		}
		else
		{
			editorUi.hideDialog();
		}	
	});
	
	applyBtn.className = 'geBtn gePrimaryBtn';

	var buttons = document.createElement('div');
	buttons.style.marginTop = '14px';
	buttons.style.textAlign = 'right';

	if (!editorUi.isOffline())
	{
		buttons.appendChild(editorUi.createHelpIcon('https://www.drawio.com/doc/faq/plugins'));
	}
	
	if (editorUi.editor.cancelFirst)
	{
		if (!closeOnly)
		{
			buttons.appendChild(cancelBtn);
		}

		buttons.appendChild(addBtn);
		buttons.appendChild(applyBtn);
	}
	else
	{
		buttons.appendChild(addBtn);
		buttons.appendChild(applyBtn);
		if (!closeOnly)
		{
			buttons.appendChild(cancelBtn);
		}
	}

	div.appendChild(buttons);

	this.container = div;
};

var CropImageDialog = function(editorUi, image, clipPath, fn)
{
	var div = document.createElement('div');
	div.style.display = 'flex';
	div.style.flexDirection = 'column';
	div.style.height = '100%';

	var croppingDiv = document.createElement('div');
	croppingDiv.style.flex = '1';
	croppingDiv.style.minHeight = '0';
	croppingDiv.style.overflow = 'auto';
	croppingDiv.style.border = '1px solid';
	
	div.appendChild(croppingDiv);

	var imageUrl = image.replace(';base64', '');
	var cropGraph = null, bgCell = null, initGeo = null,
		arcSizeVal = 5, cropCell = new mxCell('', new mxGeometry(0, 0, 1, 1), ''),
		imgW = 0, imgH = 0,
		commonStyle = 'shape=image;fillColor=none;rotatable=0;cloneable=0;deletable=0;image=' +
						imageUrl + ';clipPath=',
		bgDimStyle = 'shape=image;fillColor=none;movable=0;resizable=0;editable=0;' +
			'connectable=0;rotatable=0;deletable=0;opacity=40;image=' + imageUrl + ';',
		bgFullStyle = 'shape=image;fillColor=none;movable=0;resizable=0;editable=0;' +
			'connectable=0;rotatable=0;deletable=0;image=' + imageUrl + ';';

	function fitGraph()
	{
		if (cropGraph != null)
		{
			croppingDiv.style.overflow = 'hidden';
			cropGraph.maxFitScale = null;
			cropGraph.fit(8);
			cropGraph.center();
			croppingDiv.style.overflow = 'auto';
		}
	};

	var imgObj = new Image();
	imgObj.onload = init;
	imgObj.onerror = function()
	{
		imgObj.onload = null;
		imgObj.src = Editor.errorImage;
	};
	imgObj.src = image;

	function init()
	{
		imgW = imgObj.naturalWidth;
		imgH = imgObj.naturalHeight;

		initGeo = new mxGeometry(imgW * 0.15, imgH * 0.15,
			imgW * 0.7, imgH * 0.7);
		cropCell.geometry = initGeo.clone();

		cropGraph = new Graph(croppingDiv);
		cropGraph.autoExtend = false;
		cropGraph.autoScroll = false;
		cropGraph.setGridEnabled(false);
		cropGraph.setEnabled(true);
		cropGraph.setPanning(true);
		cropGraph.setConnectable(false);
		cropGraph.getRubberband().setEnabled(false);
		cropGraph.graphHandler.allowLivePreview = false;
		cropGraph.centerZoom = true;
		cropGraph.background = '#ffffff';

		var origCreateVertexHandler = cropGraph.createVertexHandler;

		cropGraph.createVertexHandler = function()
		{
			var handler = origCreateVertexHandler.apply(this, arguments);
			handler.livePreview = false;
			return handler;
		};

		var origIsCellSelectable = cropGraph.isCellSelectable;

		cropGraph.isCellSelectable = function(cell)
		{
			return cell === cropCell && origIsCellSelectable.apply(this, arguments);
		};

		bgCell = new mxCell('', new mxGeometry(0, 0, imgW, imgH), bgDimStyle);
		bgCell.vertex = true;
		cropGraph.addCell(bgCell);

		if (clipPath != null)
		{
			//Find position and size of cropCell
			try
			{
				if (clipPath.substring(0, 5) == 'inset')
				{
					var geo = cropCell.geometry;
					var tokens = clipPath.match(/\(([^)]+)\)/)[1].split(/[ ,]+/);

					var top = parseFloat(tokens[0]);
					var right = parseFloat(tokens[1]);
					var bottom = parseFloat(tokens[2]);
					var left = parseFloat(tokens[3]);

					if (isFinite(top) && isFinite(right) && isFinite(bottom) && isFinite(left))
					{
						geo.x = left / 100 * imgW;
						geo.y = top / 100 * imgH;
						geo.width = (100 - right) / 100 * imgW - geo.x;
						geo.height = (100 - bottom) / 100 * imgH - geo.y;

						if (tokens[4] == 'round')
						{
							if (tokens[5] == '50%')
							{
								ellipseInput.setAttribute('checked', 'checked');
							}
							else
							{
								arcSizeVal = parseInt(tokens[5]);
								arcSize.value = arcSizeVal;
								roundedInput.setAttribute('checked', 'checked');
								arcSizeDiv.style.visibility = 'visible';
							}
						}
						else
						{
							rectInput.setAttribute('checked', 'checked');
						}
					}
					else //Invalid clipPath
					{
						clipPath = null;
					}
				}
				else //The dialog supports inset only
				{
					clipPath = null;
				}
			}
			catch (e){} //Ignore
		}

		cropCell.style = getCropCellStyle(clipPath);
		cropCell.vertex = true;
		cropGraph.addCell(cropCell);
		cropGraph.selectAll();

		function updateCropCell()
		{
			cropGraph.model.setStyle(cropCell, getCropCellStyle());
			updateInputs();
		};

		cropGraph.addListener(mxEvent.CELLS_MOVED, updateCropCell);
		cropGraph.addListener(mxEvent.CELLS_RESIZED, updateCropCell);

		var origMouseUp = cropGraph.graphHandler.mouseUp;
		var origMouseDown = cropGraph.graphHandler.mouseDown;

		cropGraph.graphHandler.mouseUp = function()
		{
			origMouseUp.apply(this, arguments);

			if (bgCell != null)
			{
				cropGraph.model.setStyle(bgCell, bgDimStyle);
			}
		};

		cropGraph.graphHandler.mouseDown = function()
		{
			origMouseDown.apply(this, arguments);

			if (bgCell != null)
			{
				cropGraph.model.setStyle(bgCell, bgFullStyle);
			}
		};

		cropGraph.dblClick = function(){} //Disable text adding

		var origChangeSelection = cropGraph.getSelectionModel().changeSelection;

		//Prevent deselection
		cropGraph.getSelectionModel().changeSelection = function()
		{
			origChangeSelection.call(this, [cropCell], [cropCell]);
		};

		updateInputs();
		fitGraph();
	};

	// Zoom buttons
	var zoomBtns = document.createElement('div');
	zoomBtns.style.display = 'flex';
	zoomBtns.style.flexShrink = '0';
	zoomBtns.style.alignItems = 'center';
	zoomBtns.style.justifyContent = 'center';
	zoomBtns.style.paddingTop = '6px';

	var zoomInBtn = editorUi.createToolbarButton(Editor.zoomInImage,
		mxResources.get('zoomIn'), function()
	{
		if (cropGraph != null)
		{
			cropGraph.zoomIn()
		}
	});

	var zoomOutBtn = editorUi.createToolbarButton(Editor.zoomOutImage,
		mxResources.get('zoomOut'), function()
	{
		if (cropGraph != null)
		{
			cropGraph.zoomOut();
		}
	});

	var zoomFitBtn = editorUi.createToolbarButton(Editor.zoomFitImage,
		mxResources.get('fit'), function()
	{
		fitGraph();
	});

	zoomBtns.appendChild(zoomInBtn);
	zoomBtns.appendChild(zoomOutBtn);
	zoomBtns.appendChild(zoomFitBtn);

	var inputStyle = 'width:46px;margin:0 2px;text-align:right;';

	function createInput(label)
	{
		var lbl = document.createElement('span');
		lbl.style.marginLeft = '6px';
		lbl.style.fontSize = '11px';
		mxUtils.write(lbl, label);
		zoomBtns.appendChild(lbl);

		var input = document.createElement('input');
		input.setAttribute('type', 'text');
		input.style.cssText = inputStyle;
		zoomBtns.appendChild(input);

		return input;
	};

	var xInput = createInput('X');
	var yInput = createInput('Y');
	var wInput = createInput('W');
	var hInput = createInput('H');

	function updateInputs()
	{
		var geo = cropCell.geometry;
		xInput.value = Math.round(geo.x);
		yInput.value = Math.round(geo.y);
		wInput.value = Math.round(geo.width);
		hInput.value = Math.round(geo.height);
	};

	function applyInputs()
	{
		if (cropGraph == null) return;

		var x = parseFloat(xInput.value);
		var y = parseFloat(yInput.value);
		var w = parseFloat(wInput.value);
		var h = parseFloat(hInput.value);

		if (isFinite(x) && isFinite(y) && isFinite(w) && isFinite(h) && w > 0 && h > 0)
		{
			var geo = cropCell.geometry.clone();
			geo.x = x;
			geo.y = y;
			geo.width = w;
			geo.height = h;
			cropGraph.model.setGeometry(cropCell, geo);
			cropGraph.model.setStyle(cropCell, getCropCellStyle());
			cropGraph.selectAll();
		}
	};

	mxEvent.addListener(xInput, 'change', applyInputs);
	mxEvent.addListener(yInput, 'change', applyInputs);
	mxEvent.addListener(wInput, 'change', applyInputs);
	mxEvent.addListener(hInput, 'change', applyInputs);

	div.appendChild(zoomBtns);

	var radioDiv = document.createElement('div');
	radioDiv.style.whiteSpace = 'nowrap';
	radioDiv.style.display = 'flex';
	radioDiv.style.flexShrink = '0';
	radioDiv.style.alignItems = 'center';
	radioDiv.style.justifyContent = 'center';

	var rectInput = document.createElement('input');
	rectInput.setAttribute('type', 'radio');
	rectInput.setAttribute('id', 'croppingRect');
	rectInput.setAttribute('name', 'croppingShape');
	rectInput.setAttribute('checked', 'checked');
	rectInput.style.margin = '5px';
	radioDiv.appendChild(rectInput);

	var rectLbl = document.createElement('label');
	rectLbl.setAttribute('for', 'croppingRect');
	rectLbl.style.overflow = 'hidden';
	rectLbl.style.textOverflow = 'ellipsis';
	rectLbl.style.padding = '3px';
	mxUtils.write(rectLbl, mxResources.get('rectangle'));
	radioDiv.appendChild(rectLbl);

	var roundedInput = document.createElement('input');
	roundedInput.setAttribute('type', 'radio');
	roundedInput.setAttribute('id', 'croppingRounded');
	roundedInput.setAttribute('name', 'croppingShape');
	roundedInput.style.margin = '5px';
	radioDiv.appendChild(roundedInput);

	var roundedLbl = document.createElement('label');
	roundedLbl.setAttribute('for', 'croppingRounded');
	roundedLbl.style.overflow = 'hidden';
	roundedLbl.style.textOverflow = 'ellipsis';
	roundedLbl.style.padding = '3px';
	mxUtils.write(roundedLbl, mxResources.get('rounded'));
	radioDiv.appendChild(roundedLbl);

	var ellipseInput = document.createElement('input');
	ellipseInput.setAttribute('type', 'radio');
	ellipseInput.setAttribute('id', 'croppingEllipse');
	ellipseInput.setAttribute('name', 'croppingShape');
	ellipseInput.style.margin = '5px';
	radioDiv.appendChild(ellipseInput);

	var ellipseLbl = document.createElement('label');
	ellipseLbl.setAttribute('for', 'croppingEllipse');
	ellipseLbl.style.overflow = 'hidden';
	ellipseLbl.style.textOverflow = 'ellipsis';
	ellipseLbl.style.padding = '3px';
	mxUtils.write(ellipseLbl, mxResources.get('ellipse'));
	radioDiv.appendChild(ellipseLbl);
	div.appendChild(radioDiv);

	function calcClipPath()
	{
		var isRounded = roundedInput.checked;
		var isEllipse = ellipseInput.checked;

		var geo = cropCell.geometry;

		//prevent coords outside the image
		if (geo.x < 0)
		{
			geo.width += geo.x;
			geo.x = 0;
		}
		else if (geo.x + geo.width > imgW)
		{
			geo.width = imgW - geo.x;
			geo.x = Math.min(geo.x, imgW);
		}

		if (geo.y < 0)
		{
			geo.height += geo.y;
			geo.y = 0;
		}
		else if (geo.y + geo.height > imgH)
		{
			geo.height = imgH - geo.y;
			geo.y = Math.min(geo.y, imgH);
		}

		var left = geo.x / imgW * 100;
		var right = 100 - (geo.x + geo.width) / imgW * 100;
		var top = geo.y / imgH * 100;
		var bottom = 100 - (geo.y + geo.height) / imgH * 100;

		return 'inset(' + mxUtils.format(top) + '% ' + mxUtils.format(right) + '% ' + mxUtils.format(bottom) + '% ' + mxUtils.format(left) + '%' +
							(isRounded? ' round ' + arcSizeVal + '%' : (isEllipse? ' round 50%' : '')) + ')';
	}

	function typeChanged(noGeoReset)
	{
		if (cropGraph == null) return; //Image is not loaded yet

		if (noGeoReset !== true)
		{
			cropGraph.model.setGeometry(cropCell, initGeo.clone());
			arcSizeVal = 5;
			arcSize.value = arcSizeVal;
		}

		cropGraph.model.setStyle(cropCell, getCropCellStyle());
		cropGraph.selectAll();
		updateInputs();
		arcSizeDiv.style.visibility = roundedInput.checked ? 'visible' : 'hidden';
	}

	function getCropCellStyle(clipPath)
	{
		return commonStyle + (clipPath? clipPath : calcClipPath());
	}

	mxEvent.addListener(rectInput, 'change', typeChanged);
	mxEvent.addListener(roundedInput, 'change', typeChanged);
	mxEvent.addListener(ellipseInput, 'change', typeChanged);

	//Arc size slider
	var arcSizeDiv = document.createElement('div');
	arcSizeDiv.style.textAlign = 'center';
	arcSizeDiv.style.flexShrink = '0';
	arcSizeDiv.style.visibility = 'hidden';

	var arcSize = document.createElement('input');
	arcSize.setAttribute('type', 'range');
	arcSize.setAttribute('min', '1');
	arcSize.setAttribute('max', '49');
	arcSize.setAttribute('value', arcSizeVal);
	arcSize.setAttribute('title', mxResources.get('arcSize'));
	arcSizeDiv.appendChild(arcSize);

	div.appendChild(arcSizeDiv);

	mxEvent.addListener(arcSize, 'change', function()
	{
		arcSizeVal = this.value;
		typeChanged(true);
	});

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});

	cancelBtn.className = 'geBtn';

	var applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		fn(calcClipPath(), cropCell.geometry.width, cropCell.geometry.height);
		editorUi.hideDialog();
	});

	applyBtn.className = 'geBtn gePrimaryBtn';

	var resetBtn = mxUtils.button(mxResources.get('reset'), function()
	{
		fn(null, imgW, imgH);
		editorUi.hideDialog();
	});

	resetBtn.className = 'geBtn';

	var buttons = document.createElement('div');
	buttons.style.flexShrink = '0';
	buttons.style.marginTop = '10px';
	buttons.style.whiteSpace = 'nowrap';
	buttons.style.display = 'flex';
	buttons.style.justifyContent = 'flex-end';

	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
		buttons.appendChild(resetBtn);
		buttons.appendChild(applyBtn);
	}
	else
	{
		buttons.appendChild(resetBtn);
		buttons.appendChild(applyBtn);
		buttons.appendChild(cancelBtn);
	}

	div.appendChild(buttons);

	this.container = div;
};

var EditGeometryDialog = function(editorUi, vertices) 
{
	var graph = editorUi.editor.graph;
	var geo = (vertices.length == 1) ? graph.getCellGeometry(vertices[0]) : null;
	var div = document.createElement('div');
	
	var table = document.createElement('table');
	var tbody = document.createElement('tbody');
	var row = document.createElement('tr');
	var left = document.createElement('td');
	var right = document.createElement('td');
	table.style.paddingLeft = '6px';
	
	mxUtils.write(left, mxResources.get('relative') + ':');
	
	var relInput = document.createElement('input');
	relInput.setAttribute('type', 'checkbox');
	
	if (geo != null && geo.relative)
	{
		relInput.setAttribute('checked', 'checked');
		relInput.defaultChecked = true;
	}
	
	this.init = function()
	{
		relInput.focus();
	};

	right.appendChild(relInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('left') + ':');
	
	var xInput = document.createElement('input');
	xInput.setAttribute('type', 'text');
	xInput.style.width = '100px';
	xInput.value = (geo != null) ? geo.x : '';

	right.appendChild(xInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('top') + ':');
	
	var yInput = document.createElement('input');
	yInput.setAttribute('type', 'text');
	yInput.style.width = '100px';
	yInput.value = (geo != null) ? geo.y : '';

	right.appendChild(yInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('dx') + ':');
	
	var dxInput = document.createElement('input');
	dxInput.setAttribute('type', 'text');
	dxInput.style.width = '100px';
	dxInput.value = (geo != null && geo.offset != null) ? geo.offset.x : '';

	right.appendChild(dxInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('dy') + ':');
	
	var dyInput = document.createElement('input');
	dyInput.setAttribute('type', 'text');
	dyInput.style.width = '100px';
	dyInput.value = (geo != null && geo.offset != null) ? geo.offset.y : '';

	right.appendChild(dyInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('width') + ':');
	
	var wInput = document.createElement('input');
	wInput.setAttribute('type', 'text');
	wInput.style.width = '100px';
	wInput.value = (geo != null) ? geo.width : '';

	right.appendChild(wInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('height') + ':');
	
	var hInput = document.createElement('input');
	hInput.setAttribute('type', 'text');
	hInput.style.width = '100px';
	hInput.value = (geo != null) ? geo.height : '';

	right.appendChild(hInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	left = document.createElement('td');
	right = document.createElement('td');
	
	mxUtils.write(left, mxResources.get('rotation') + ':');
	
	var rotInput = document.createElement('input');
	rotInput.setAttribute('type', 'text');
	rotInput.style.width = '100px';
	rotInput.value = (vertices.length == 1) ? mxUtils.getValue(graph.getCellStyle(vertices[0]),
			mxConstants.STYLE_ROTATION, 0) : '';

	right.appendChild(rotInput);

	row.appendChild(left);
	row.appendChild(right);
	
	tbody.appendChild(row);
	
	table.appendChild(tbody);
	div.appendChild(table);
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	
	cancelBtn.className = 'geBtn';
	
	var applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		editorUi.hideDialog();
		
		graph.getModel().beginUpdate();
		try
		{
			for (var i = 0; i < vertices.length; i++)
			{
				var g = graph.getCellGeometry(vertices[i]);
				
				if (g != null)
				{
					g = g.clone();
				
					if (graph.isCellMovable(vertices[i]))
					{
						g.relative = relInput.checked;
						
						if (mxUtils.trim(xInput.value).length > 0)
						{
							g.x = Number(xInput.value);
						}
						
						if (mxUtils.trim(yInput.value).length > 0)
						{
							g.y = Number(yInput.value);
						}
						
						if (mxUtils.trim(dxInput.value).length > 0)
						{
							if (g.offset == null)
							{
								g.offset = new mxPoint();
							}
							
							g.offset.x = Number(dxInput.value);
						}
						
						if (mxUtils.trim(dyInput.value).length > 0)
						{
							if (g.offset == null)
							{
								g.offset = new mxPoint();
							}
							
							g.offset.y = Number(dyInput.value);
						}
					}
					
					if (graph.isCellResizable(vertices[i]))
					{
						if (mxUtils.trim(wInput.value).length > 0)
						{
							g.width = Number(wInput.value);
						}
						
						if (mxUtils.trim(hInput.value).length > 0)
						{
							g.height = Number(hInput.value);
						}
					}
					
					graph.getModel().setGeometry(vertices[i], g);
				}
				
				if (mxUtils.trim(rotInput.value).length > 0)
				{
					graph.setCellStyles(mxConstants.STYLE_ROTATION, Number(rotInput.value), [vertices[i]]);
				}
			}
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	});
	
	applyBtn.className = 'geBtn gePrimaryBtn';
	
	mxEvent.addListener(div, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			applyBtn.click();
		}
	});
	
	var buttons = document.createElement('div');
	buttons.style.marginTop = '20px';
	buttons.style.textAlign = 'right';

	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
		buttons.appendChild(applyBtn);
	}
	else
	{
		buttons.appendChild(applyBtn);
		buttons.appendChild(cancelBtn);
	}

	div.appendChild(buttons);

	this.container = div;
};

/**
 * Constructs a new dialog for creating files from templates.
 */
var LibraryDialog = function(editorUi, name, library, initialImages, file, mode, allowBrowser)
{
	var images = [];
	var graph = editorUi.editor.graph;
	var outer = document.createElement('div');
	outer.style.height = '100%';
	
	var header = document.createElement('div');
	header.style.whiteSpace = 'nowrap';
	header.style.height = '40px';
	outer.appendChild(header);

	mxUtils.write(header, mxResources.get('filename') + ':');
	
	var nameValue = name;
	
	if (nameValue == null)
	{
		nameValue = editorUi.defaultLibraryName + '.xml';
	}

	var nameInput = document.createElement('input');
	nameInput.setAttribute('value', nameValue);
	nameInput.style.marginRight = '20px';
	nameInput.style.marginLeft = '10px';
	nameInput.style.width = '500px';
	
	if (file != null && !file.isRenamable())
	{
		nameInput.setAttribute('disabled', 'true');
	}
	
	this.init = function()
	{
		if (file == null || file.isRenamable())
		{
			nameInput.focus();
			
			if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
			{
				nameInput.select();
			}
			else
			{
				document.execCommand('selectAll', false, null);
			}
		}
	};

	header.appendChild(nameInput);

	if (Editor.enableUncompressedLibraries)
	{
		nameInput.style.width = '420px';
		var compressedInput = document.createElement('input');
		compressedInput.setAttribute('type', 'checkbox');
		compressedInput.style.marginRight = '10px';
		header.appendChild(compressedInput);
		mxUtils.write(header, mxResources.get('compressed'));
	}

	var div = document.createElement('div');
	div.style.borderWidth = '1px 0px 1px 0px';
	div.style.borderColor = '#d3d3d3';
	div.style.borderStyle = 'solid';
	div.style.marginTop = '6px';
	div.style.overflow = 'auto';
	div.style.height = '340px';
	div.style.backgroundPosition = 'center center';
	div.style.backgroundRepeat = 'no-repeat';

	if (images.length == 0 && Graph.fileSupport)
	{
		div.style.backgroundImage = 'url(\'' + IMAGE_PATH + '/droptarget.png\')';
	}

	var bg = document.createElement('div');
	bg.style.position = 'absolute';
	bg.style.width = '640px';
	bg.style.top = '260px';
	bg.style.textAlign = 'center';
	bg.style.fontSize = '22px';
	bg.style.color = '#a0c3ff';
	mxUtils.write(bg, mxResources.get('dragImagesHere'));
	outer.appendChild(bg);
	
	var entries = {};
	var ew = 100;
	var eh = 100;
	
	var dragSourceIndex = null;
	var dropTargetIndex = null;
	
	function getIndexForEvent(evt)
	{
		var dropTarget = document.elementFromPoint(evt.clientX, evt.clientY);
		
		while (dropTarget != null && dropTarget.parentNode != div)
		{
			dropTarget = dropTarget.parentNode;
		}
		
		var result = null;
		
		if (dropTarget != null)
		{
			var tmp = div.firstChild;
			result = 0;
			
			while (tmp != null && tmp != dropTarget)
			{
				tmp = tmp.nextSibling;
				result++;
			}
		}
		
		return result;
	};
	
	var stopEditing = null;
	var stopWrapper = function(evt)
	{
		var source = mxEvent.getSource(evt);
		
		if (source.getAttribute('contentEditable') != 'true' && stopEditing != null)
		{
			stopEditing();
			stopEditing = null;
			
			mxEvent.consume(evt);
		}
	};
	
	mxEvent.addListener(div, 'mousedown', stopWrapper);
	mxEvent.addListener(div, 'pointerdown', stopWrapper);
	mxEvent.addListener(div, 'touchstart', stopWrapper);

	// For converting image URLs
	var converter = new mxUrlConverter();
	var errorShowed = false;
	
	function addButton(data, mimeType, x, y, w, h, img, aspect, title)
	{
		// Ignores duplicates
		try
		{
			editorUi.spinner.stop();
			
			if (mimeType == null || mimeType.substring(0, 6) == 'image/')
			{
				if ((data == null && img != null) || entries[data] == null)
				{
					div.style.backgroundImage = '';
					bg.style.display = 'none';
		
					var iw = w;
					var ih = h;
					
					if (w > editorUi.maxImageSize || h > editorUi.maxImageSize)
					{
						var s = Math.min(1, Math.min(editorUi.maxImageSize / Math.max(1, w)),
							editorUi.maxImageSize / Math.max(1, h));
						w *= s;
						h *= s;
					}
					
					if (iw > ih)
					{
						ih = Math.round(ih * ew / iw);
						iw = ew;
					}
					else
					{
						iw = Math.round(iw * eh / ih);
						ih = eh;
					}
					
					var wrapper = document.createElement('div');
					wrapper.setAttribute('draggable', 'true');
					wrapper.style.display = 'inline-block';
					wrapper.style.position = 'relative';
					wrapper.style.padding = '0 12px';
					wrapper.style.cursor = 'move';
					mxUtils.setPrefixedStyle(wrapper.style, 'transition', 'transform .1s ease-in-out');
					
					if (data != null)
					{
						var elt = document.createElement('img');
						elt.setAttribute('src', converter.convert(data));
						elt.style.width = iw + 'px';
						elt.style.height = ih + 'px';
						elt.style.margin = '10px';
			
						elt.style.paddingBottom = Math.floor((eh - ih) / 2) + 'px';
						elt.style.paddingLeft = Math.floor((ew - iw) / 2) + 'px';
						
						wrapper.appendChild(elt);
					}
					else if (img != null)
					{
						var cells = editorUi.stringToCells((img.xml.charAt(0) == '<') ?
							img.xml : Graph.decompress(img.xml));
						
						if (cells.length > 0)
						{
							editorUi.sidebar.createThumb(cells, ew, eh, wrapper, null,
								true, false, null, null, graph.shapeBackgroundColor);
							
							// Needs inline block on SVG for delete icon to appear on same line
							wrapper.firstChild.style.display = 'inline-block';
							wrapper.firstChild.style.cursor = '';
						}
					}
					
					var rem = document.createElement('img');
					rem.setAttribute('src', Editor.closeBlackImage);
					rem.setAttribute('border', '0');
					rem.setAttribute('title', mxResources.get('delete'));
					rem.setAttribute('align', 'top');
					rem.style.paddingTop = '4px';
					rem.style.position = 'absolute';
					rem.style.marginLeft = '-12px';
					rem.style.zIndex = '1';
					rem.style.cursor = 'pointer';
					
					// Blocks dragging of remove icon
					mxEvent.addListener(rem, 'dragstart', function(evt)
					{
						mxEvent.consume(evt);
					});
					
					(function(wrapperDiv, dataParam, imgParam)
					{
						mxEvent.addListener(rem, 'click', function(evt)
						{
							entries[dataParam] = null;
							
							for (var i = 0; i < images.length; i++)
							{
								if ((images[i].data != null && images[i].data == dataParam) ||
									(images[i].xml != null && imgParam != null &&
									images[i].xml == imgParam.xml))
								{
									images.splice(i, 1);
									break;
								}
							}
							
							wrapper.parentNode.removeChild(wrapperDiv);
							
							if (images.length == 0)
							{
								div.style.backgroundImage = 'url(\'' + IMAGE_PATH + '/droptarget.png\')';
								bg.style.display = '';
							}
							
							mxEvent.consume(evt);
						});
						// Workaround for accidental select all
						mxEvent.addListener(rem, 'dblclick', function(evt)
						{
							mxEvent.consume(evt);
						});
					})(wrapper, data, img);
					
					wrapper.appendChild(rem);
					wrapper.style.marginBottom = '30px';
					
					var label = document.createElement('div');
					label.style.position = 'absolute';
					label.style.boxSizing = 'border-box';
					label.style.bottom = '-18px';
					label.style.left = '10px';
					label.style.right = '10px';
					label.style.backgroundColor = 'light-dark(#ffffff, transparent)';
					label.style.overflow = 'hidden';
					label.style.textAlign = 'center';
					label.setAttribute('title', mxResources.get('rename'));
					
					var entry = null;
					
					if (data != null)
					{
						entry = {data: data, w: w, h: h, title: title};
						
						if (aspect != null)
						{
							entry.aspect = aspect;
						}
						
						entries[data] = elt;
						images.push(entry);
					}
					else if (img != null)
					{
						img.aspect = 'fixed';
						images.push(img);
						entry = img;
					}
					
					function updateLabel()
					{
						label.innerText = '';
						label.style.cursor = 'pointer';
						label.style.whiteSpace = 'nowrap';
						label.style.textOverflow = 'ellipsis';
						mxUtils.write(label, (entry.title != null && entry.title.length > 0) ?
							entry.title : mxResources.get('untitled'));
						
						if (entry.title == null || entry.title.length == 0)
						{
							label.style.color = '#d0d0d0';
						}
						else
						{
							label.style.color = '';
						}
					};
					
					mxEvent.addListener(label, 'keydown', function(evt)
					{
						if (evt.keyCode == 13 && stopEditing != null)
						{
							stopEditing();
							stopEditing = null;
							
							mxEvent.consume(evt);
						}
					});
					
					updateLabel();
					wrapper.appendChild(label);
					
					// Blocks dragging of label
					mxEvent.addListener(label, 'mousedown', function(evt)
					{
						if (label.getAttribute('contentEditable') != 'true')
						{
							mxEvent.consume(evt);
						}
					});
					
					var startEditing = function(evt)
					{
						// Workaround for various issues in IE
						if (!mxClient.IS_IOS && !mxClient.IS_FF &&
							(document.documentMode == null || document.documentMode > 9))
						{
							if (label.getAttribute('contentEditable') != 'true')
							{
								if (stopEditing != null)
								{
									stopEditing();
									stopEditing = null;
								}
								
								if (entry.title == null || entry.title.length == 0)
								{
									label.innerText = '';
								}
								
								label.style.textOverflow = '';
								label.style.whiteSpace = '';
								label.style.cursor = 'text';
								label.style.color = '';
								label.setAttribute('contentEditable', 'true');
								mxUtils.setPrefixedStyle(label.style, 'user-select', 'text');
								label.focus();
								document.execCommand('selectAll', false, null);
								
								stopEditing = function()
								{
									label.removeAttribute('contentEditable');
									label.style.cursor = 'pointer';
									entry.title = mxUtils.getTextContent(label);
									updateLabel();
								}
						
								mxEvent.consume(evt);
							}
						}
						else
						{
							var dlg = new FilenameDialog(editorUi, entry.title || '',
								mxResources.get('ok'), function(newTitle)
							{
								if (newTitle != null)
								{
									entry.title = newTitle;
									updateLabel();
								}
							}, mxResources.get('enterValue'));
							editorUi.showDialog(dlg.container, 300, 80, true, true);
							dlg.init();
							
							mxEvent.consume(evt);
						}
					};
					
					mxEvent.addListener(label, 'click', startEditing);
					mxEvent.addListener(wrapper, 'dblclick', startEditing);
					
					div.appendChild(wrapper);
	
					mxEvent.addListener(wrapper, 'dragstart', function(evt)
					{
						if (stopEditing != null)
						{
							return;
						}

						if (data == null && img != null)
						{
							rem.style.visibility = 'hidden';
							label.style.visibility = 'hidden';
						}
						
						// Workaround for no DnD on DIV in FF
						if (mxClient.IS_FF && img.xml != null)
						{
							evt.dataTransfer.setData('Text', img.xml);
						}

						dragSourceIndex = getIndexForEvent(evt);
						
						// Workaround for missing drag preview in Google Chrome
						if (mxClient.IS_GC)
						{
							wrapper.style.opacity = '0.9';
						}
						
						window.setTimeout(function()
						{
							mxUtils.setPrefixedStyle(wrapper.style, 'transform', 'scale(0.5,0.5)');
							mxUtils.setOpacity(wrapper, 30);
							rem.style.visibility = '';
							label.style.visibility = '';
						}, 0);
					});
					
					mxEvent.addListener(wrapper, 'dragend', function(evt)
					{
						if (rem.style.visibility == 'hidden')
						{
							rem.style.visibility = '';
							label.style.visibility = '';
						}
						
						dragSourceIndex = null;
						mxUtils.setOpacity(wrapper, 100);
						mxUtils.setPrefixedStyle(wrapper.style, 'transform', null);
					});
				}
				else if (!errorShowed)
				{
					errorShowed = true;
					editorUi.handleError({message: mxResources.get('fileExists')})
				}
			}
			else
			{
				var done = false;
				
				try
				{
					var doc = mxUtils.parseXml(data);
					
					if (doc.documentElement.nodeName == 'mxlibrary')
					{
						var temp = JSON.parse(mxUtils.getTextContent(doc.documentElement));
							
						if (temp != null && temp.length > 0)
						{
							for (var i = 0; i < temp.length; i++)
							{
								if (temp[i].xml != null)
								{
									addButton(null, null, 0, 0, 0, 0, temp[i]);
								}
								else
								{
									addButton(temp[i].data, null, 0, 0, temp[i].w, temp[i].h, null, 'fixed', temp[i].title);
								}
							}
						}
						
						done = true;
					}
					else if (doc.documentElement.nodeName == 'mxfile')
					{
						var pages = doc.documentElement.getElementsByTagName('diagram');
						
						for (var i = 0; i < pages.length; i++)
						{
							var xml = Editor.getDiagramNodeXml(pages[i]);
							var cells = editorUi.stringToCells(xml);

							if (cells.length > 0)
							{
								var size = editorUi.editor.graph.getBoundingBoxFromGeometry(cells);

								if (size != null)
								{
									addButton(null, null, 0, 0, 0, 0, {xml: xml, w: size.width, h: size.height});
								}
							}
						}
						
						done = true;
					}
				}
				catch (e)
				{
					if (window.console != null)
					{
						console.error('Error in library dialog: ' + e);
					}
				}

				if (!done)
				{
					editorUi.spinner.stop();
					editorUi.handleError({message: mxResources.get('errorLoadingFile')})
				}
			}
		}
		catch (e)
		{
			if (window.console != null)
			{
				console.log('Error in library dialog: ' + e);
			}
		}
		
		return null;
	};
	
	if (initialImages != null)
	{
		for (var i = 0; i < initialImages.length; i++)
		{
			var img = initialImages[i];
			addButton(img.data, null, 0, 0, img.w, img.h, img, img.aspect, img.title);
		}
	}
	
	// Setup the dnd listeners
	mxEvent.addListener(div, 'dragleave', function(evt)
	{
		bg.style.cursor = '';
		var source = mxEvent.getSource(evt);
		
		while (source != null)
		{
			if (source == div || source == bg)
			{
				evt.stopPropagation();
				evt.preventDefault();
				break;
			}
			
			source = source.parentNode;
		}
	});
	
	function dragOver(evt)
	{
		evt.dataTransfer.dropEffect = (dragSourceIndex != null) ? 'move' : 'copy';
		evt.stopPropagation();
		evt.preventDefault();
	};
	
	var createImportHandler = function(evt)
	{
		return function(data, mimeType, x, y, w, h, img, doneFn, file)
		{
			if (file != null && EditorUi.isVisioFilename(file.name))
			{
				editorUi.importVisio(file, mxUtils.bind(this, function(xml)
				{
		    		addButton(xml, mimeType, x, y, w, h, img, 'fixed', (mxEvent.isAltDown(evt)) ?
		    			null : img.substring(0, img.lastIndexOf('.')).replace(/_/g, ' '));
				}));
			}
			else if (file != null && new XMLHttpRequest().upload && editorUi.isRemoteFileFormat(data, file.name))
			{
				if (editorUi.isExternalDataComms())
				{
					editorUi.parseFile(file, mxUtils.bind(this, function(xhr)
					{
						if (xhr.readyState == 4)
						{
							editorUi.spinner.stop();
							
							if (xhr.status >= 200 && xhr.status <= 299)
							{
								var xml = xhr.responseText;
								addButton(xml, mimeType, x, y, w, h, img, 'fixed', (mxEvent.isAltDown(evt)) ?
									null : img.substring(0, img.lastIndexOf('.')).replace(/_/g, ' '));
								div.scrollTop = div.scrollHeight;
							}
						}
					}));
				}
				else
				{
					editorUi.spinner.stop();
					editorUi.showError(mxResources.get('error'), mxResources.get('notInOffline'));
				}
			}
			else
			{
				addButton(data, mimeType, x, y, w, h, img, 'fixed', (mxEvent.isAltDown(evt)) ?
					null : img.substring(0, img.lastIndexOf('.')).replace(/_/g, ' '));
				div.scrollTop = div.scrollHeight;
			}
		};
	};
	
	function dropHandler(evt)
	{
		evt.stopPropagation();
		evt.preventDefault();
		errorShowed = false;
		dropTargetIndex = getIndexForEvent(evt);
		
		if (dragSourceIndex != null)
		{
	    	if (dropTargetIndex != null && dropTargetIndex < div.children.length)
	    	{
				images.splice((dropTargetIndex > dragSourceIndex) ? dropTargetIndex - 1 : dropTargetIndex,
					0, images.splice(dragSourceIndex, 1)[0]);
				div.insertBefore(div.children[dragSourceIndex], div.children[dropTargetIndex]);
			}
			else
			{
				images.push(images.splice(dragSourceIndex, 1)[0]);
				div.appendChild(div.children[dragSourceIndex]);
			}
		}
		else if (evt.dataTransfer.files.length > 0)
		{
			editorUi.importFiles(evt.dataTransfer.files, 0, 0, editorUi.maxImageSize, createImportHandler(evt));
		}
		else if (mxUtils.indexOf(evt.dataTransfer.types, 'text/uri-list') >= 0)
		{
			var uri = decodeURIComponent(evt.dataTransfer.getData('text/uri-list'));
			
			if (/(\.jpg)($|\?)/i.test(uri) || /(\.png)($|\?)/i.test(uri) ||
				/(\.gif)($|\?)/i.test(uri) || /(\.svg)($|\?)/i.test(uri))
			{
				editorUi.loadImage(uri, function(img)
				{
					addButton(uri, null, 0, 0, img.width, img.height);
					div.scrollTop = div.scrollHeight;
				});
			}
		}
		
		evt.stopPropagation();
		evt.preventDefault();
	};
	
	mxEvent.addListener(div, 'dragover', dragOver);
	mxEvent.addListener(div, 'drop', dropHandler);
	mxEvent.addListener(bg, 'dragover', dragOver);
	mxEvent.addListener(bg, 'drop', dropHandler);

	outer.appendChild(div);

	var btns = document.createElement('div');
	btns.style.textAlign = 'right';
	btns.style.marginTop = '20px';

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog(true);
	});
	
	cancelBtn.setAttribute('id', 'btnCancel');
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	if (editorUi.getServiceName() == 'draw.io' && file != null &&
		// Limits button to ibraries which are known to have public URLs
		(file.constructor == DriveLibrary || file.constructor == GitHubLibrary))
	{
		var btn = mxUtils.button(mxResources.get('link'), function()
		{
			editorUi.getPublicUrl(file, function(url)
			{
				if (url != null)
				{
					var search = editorUi.getSearch(['create', 'title', 'mode', 'url', 'drive', 'splash', 'state', 'clibs', 'ui']);
					search += ((search.length == 0) ? '?' : '&') + 'splash=0&clibs=U' + encodeURIComponent(url);
					var dlg = new EmbedDialog(editorUi, window.location.protocol + '//' +
						window.location.host + '/' + search, null, null, null, null,
						'Check out the library I made using @drawio');
					editorUi.showDialog(dlg.container, 450, 270, true, true, null,
						false, null, new mxRectangle(0, 0, 400, 250));
					dlg.init();
				}
				else if (file.constructor == DriveLibrary)
				{
					editorUi.showError(mxResources.get('error'), mxResources.get('diagramIsNotPublic'),
						mxResources.get('share'), mxUtils.bind(this, function()
						{
							editorUi.drive.showPermissions(file.getId(), file);
						}), null, mxResources.get('ok'), mxUtils.bind(this, function()
						{
							// Hides dialog
						})
					);
				}
				else
				{
					editorUi.handleError({message: mxResources.get('diagramIsNotPublic')});
				}
			});
		});

		btn.className = 'geBtn';
		btns.appendChild(btn);
	}
	
	var btn = mxUtils.button(mxResources.get('export'), function()
	{
    	var data = editorUi.createLibraryDataFromImages(images);
    	var filename = nameInput.value;
	    	
		if (!/(\.xml)$/i.test(filename))
		{
			filename += '.xml';
		}
	    	
    	if (editorUi.isLocalFileSave())
    	{
    		editorUi.saveLocalFile(data, filename, 'text/xml', null, null, allowBrowser != null? allowBrowser : true, null, 'xml');
    	}
    	else
    	{
    		new mxXmlRequest(SAVE_URL, 'filename=' + encodeURIComponent(filename) +
    			'&format=xml&xml=' + encodeURIComponent(data)).simulate(document, '_blank');
    	}
	});
	
	btn.setAttribute('id', 'btnDownload');
	btn.className = 'geBtn';
	btns.appendChild(btn);
	
	if (Graph.fileSupport)
	{
		if (editorUi.libDlgFileInputElt == null) 
		{
			var fileInput = document.createElement('input');
			fileInput.setAttribute('multiple', 'multiple');
			fileInput.setAttribute('type', 'file');
	
			mxEvent.addListener(fileInput, 'change', function(evt)
			{
		    	errorShowed = false;

		    	editorUi.importFiles(fileInput.files, 0, 0, editorUi.maxImageSize, function(data, mimeType, x, y, w, h, img, doneFn, file)
		    	{
					if (fileInput.files != null)
					{
			    		createImportHandler(evt)(data, mimeType, x, y, w, h, img, doneFn, file);
		
			    		// Resets input to force change event for same file (type reset required for IE)
			    		fileInput.type = '';
			    		fileInput.type = 'file';
			    		fileInput.value = '';
					}
		    	});
	
				div.scrollTop = div.scrollHeight;
			});
			
			fileInput.style.display = 'none';
			document.body.appendChild(fileInput);
			editorUi.libDlgFileInputElt = fileInput;
		}
		
		var btn = mxUtils.button(mxResources.get('import'), function()
		{
			if (stopEditing != null)
			{
				stopEditing();
				stopEditing = null;
			}
			
			editorUi.libDlgFileInputElt.click();
		});
		btn.setAttribute('id', 'btnAddImage');
		btn.className = 'geBtn';
		
		btns.appendChild(btn);
	}

	var btn = mxUtils.button(mxResources.get('addImages'), function()
	{
		if (stopEditing != null)
		{
			stopEditing();
			stopEditing = null;
		}
		
		editorUi.showImageDialog(mxResources.get('addImageUrl'), '', function(url, w, h)
		{
			errorShowed = false;
			
			if (url != null)
			{
				// Image dialog returns modified data URLs which
				// must be converted back to real data URL
				if (url.substring(0, 11) == 'data:image/')
				{
					var comma = url.indexOf(',');
					
					if (comma > 0)
					{
						url = url.substring(0, comma) + ';base64,' + url.substring(comma + 1);
					}
				}
				
				addButton(url, null, 0, 0, w, h);
				div.scrollTop = div.scrollHeight;
			}
		});
	});
	
	btn.setAttribute('id', 'btnAddImageUrl');
	btn.className = 'geBtn';
	btns.appendChild(btn);
	
	// Indirection for overriding
	this.saveBtnClickHandler = function(name, images, file, mode) 
	{
		editorUi.saveLibrary(name, images, file, mode);
	};
	
	var btn = mxUtils.button(mxResources.get('save'),mxUtils.bind(this, function()
	{
		if (stopEditing != null)
		{
			stopEditing();
			stopEditing = null;
		}
		
		this.saveBtnClickHandler(nameInput.value, images, file, mode);
	}));
	
	btn.setAttribute('id', 'btnSave');
	btn.className = 'geBtn gePrimaryBtn';
	btns.appendChild(btn);
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	outer.appendChild(btns);
	
	this.container = outer;
};

/**
 * Constructs a new textarea dialog.
 */
var EditShapeDialog = function(editorUi, cell, title)
{
	var div = document.createElement('div');
	div.style.position = 'absolute';
	div.style.left = '30px';
	div.style.right = '30px';
	div.style.top = '30px';
	div.style.bottom = '30px';

	var titleDiv = document.createElement('div');
	titleDiv.style.position = 'absolute';
	titleDiv.style.top = '0px';
	mxUtils.write(titleDiv, title);
	div.appendChild(titleDiv);

	var contentDiv = document.createElement('div');
	contentDiv.style.position = 'absolute';
	contentDiv.style.width = '100%';
	contentDiv.style.display = 'flex';
	contentDiv.style.alignItems = 'stretch';
	contentDiv.style.top = '24px';
	contentDiv.style.bottom = '50px';

	var textarea = document.createElement('textarea');
	textarea.style.outline = 'none';
	textarea.style.resize = 'horizontal';
	textarea.style.width = '430px';
	textarea.style.maxWidth = 'calc(100% - 100px)';
	textarea.style.flexShrink = '0';
	textarea.style.borderRadius = '4px';
	textarea.style.marginRight = '4px';
	contentDiv.appendChild(textarea);

	var previewDiv = document.createElement('div');
	previewDiv.style.borderWidth = '1px';
	previewDiv.style.borderStyle = 'solid';
	previewDiv.style.padding = '20px';
	previewDiv.style.flexGrow = '1';
	previewDiv.style.borderRadius = '4px';
	mxEvent.disableContextMenu(previewDiv);
	contentDiv.appendChild(previewDiv);

	var graph = new Graph(previewDiv);
	graph.setEnabled(false);

	var clone = editorUi.editor.graph.cloneCell(cell);
	graph.addCells([clone]);
	
	var state = graph.view.getState(clone);
	var stencil = '';
	
	if (state.shape != null && state.shape.stencil != null)
	{
		stencil = mxUtils.getPrettyXml(state.shape.stencil.desc);
	}
	
	mxUtils.write(textarea, stencil || '');
	div.appendChild(contentDiv);

	var btns = document.createElement('div');
	btns.style.position = 'absolute';
	btns.style.display = 'flex';
	btns.style.alignItems = 'center';
	btns.style.justifyContent = 'end';
	btns.style.bottom = '6px';
	btns.style.height = '30px';
	btns.style.width = '100%';
	
	if (!editorUi.isOffline())
	{
		btns.appendChild(editorUi.createHelpIcon(
			'https://www.drawio.com/doc/faq/shape-complex-create-edit'));
	}
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	var updateShape = function(targetGraph, targetCell, hide)
	{
		var newValue = textarea.value;
		
		// Checks if XML has changed (getPrettyXml "normalizes" DOM)
		var doc = mxUtils.parseXml(newValue);
		newValue = mxUtils.getPrettyXml(doc.documentElement);
		
		// Checks for validation errors
		// LATER: Validate against XSD
		var errors = doc.documentElement.getElementsByTagName('parsererror');
		
		if (errors != null && errors.length > 0)
		{
			editorUi.showError(mxResources.get('error'), mxResources.get('containsValidationErrors'), mxResources.get('ok'));
		}
		else
		{
			if (hide)
			{
				editorUi.hideDialog();
			}
			
			var isNew = !targetGraph.model.contains(targetCell);
			
			if (!hide || isNew || newValue != stencil)
			{
				// Transform XML value to be used in cell style
				newValue = Graph.compress(newValue);
				
				targetGraph.getModel().beginUpdate();
				try
				{
					// Inserts cell if required
					if (isNew)
					{
						var pt = editorUi.editor.graph.getFreeInsertPoint();
						targetCell.geometry.x = pt.x;
						targetCell.geometry.y = pt.y;
						targetGraph.addCell(targetCell)
					}
					
					targetGraph.setCellStyles(mxConstants.STYLE_SHAPE, 'stencil(' + newValue + ')', [targetCell]);
				}
				catch (e)
				{
					throw e;
				}
				finally
				{
					// Updates the display
					targetGraph.getModel().endUpdate();
				}
				
				// Updates selection after stencil was created for rendering
				if (isNew)
				{
					targetGraph.setSelectionCell(targetCell);
					targetGraph.scrollCellToVisible(targetCell);
				}
			}
		}
	};
	
	var previewBtn = mxUtils.button(mxResources.get('preview'), function()
	{
		updateShape(graph, clone, false);
		graph.fit();
	});
	
	previewBtn.className = 'geBtn';	
	btns.appendChild(previewBtn);
	
	var applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		updateShape(editorUi.editor.graph, cell, true);
	});
	
	applyBtn.className = 'geBtn gePrimaryBtn';	
	btns.appendChild(applyBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	div.appendChild(btns);

	this.init = function()
	{
		textarea.focus();
		textarea.scrollTop = 0;
		graph.fit();
		previewDiv.style.overflow = 'auto';
		graph.fit();
	};

	this.container = div;
};

var CustomDialog = function(editorUi, content, okFn, cancelFn, okButtonText, helpLink,
		buttonsContent, hideCancel, cancelButtonText, hideAfterOKFn, customButtons,
		marginTop)
{
	var div = document.createElement('div');
	div.appendChild(content);
	
	var btns = document.createElement('div');
	btns.style.marginTop = (marginTop != null) ? marginTop : '30px';
	btns.style.textAlign = 'right';
	
	if (buttonsContent != null)
	{
		btns.appendChild(buttonsContent);
	}
	
	if (!editorUi.isOffline() && helpLink != null)
	{	
		btns.appendChild(editorUi.createHelpIcon(helpLink));
	}
	
	var cancelBtn = mxUtils.button(cancelButtonText || mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
		
		if (cancelFn != null)
		{
			cancelFn();
		}
	});
	
	cancelBtn.className = 'geBtn';
	
	if (hideCancel)
	{
		cancelBtn.style.display = 'none';
	}
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	var okBtn = mxUtils.button(okButtonText || mxResources.get('ok'), mxUtils.bind(this, function()
	{
		if (!hideAfterOKFn)
		{
			editorUi.hideDialog(null, null, this.container);
		}
		
		if (okFn != null)
		{
			var okRet = okFn();
			
			if (typeof okRet === 'string')
			{
				editorUi.showError(mxResources.get('error'), okRet);
				return;	
			}
		}
		
		if (hideAfterOKFn)
		{
			editorUi.hideDialog(null, null, this.container);
		}
	}));
	btns.appendChild(okBtn);
	
	okBtn.className = 'geBtn gePrimaryBtn';
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	if (customButtons != null)
	{
		for (var i = 0; i < customButtons.length; i++)
		{
			(function(label, fn, title)
			{
				var customBtn = mxUtils.button(label, function(e)
				{
					fn(e);
				});

				if (title != null)
				{
					customBtn.setAttribute('title', title);
				}

				customBtn.className = 'geBtn';
				btns.appendChild(customBtn);
			})(customButtons[i][0], customButtons[i][1], customButtons[i][2]);
		}
	}
	
	div.appendChild(btns);

	this.cancelBtn = cancelBtn;
	this.okButton = okBtn;
	this.container = div;
};

/**
 * Constructs a new popup opener button dialog.
 */
var BtnDialog = function(editorUi, peer, btnLbl, fn)
{
	var div = document.createElement('div');
	div.style.textAlign = 'center';
	
	var hd = document.createElement('p');
	hd.style.fontSize = '16pt';
	hd.style.padding = '0px';
	hd.style.margin = '0px';
	hd.style.color = 'gray';
	
	mxUtils.write(hd, mxResources.get('done'));
	
	var service = 'Unknown';
	
	var img = document.createElement('img');
	img.setAttribute('border', '0');
	img.setAttribute('align', 'absmiddle');
	img.style.marginRight = '10px';

	if (peer == editorUi.drive)
	{
		service = mxResources.get('googleDrive');
		img.src = IMAGE_PATH + '/google-drive-logo-white.svg';
	}
	else if (peer == editorUi.dropbox)
	{
		service = mxResources.get('dropbox');
		img.src = IMAGE_PATH + '/dropbox-logo-white.svg';
	}
	else if (peer == editorUi.oneDrive)
	{
		service = mxResources.get('oneDrive');
		img.src = IMAGE_PATH + '/onedrive-logo-white.svg';
	}
	else if (peer == editorUi.gitHub)
	{
		service = mxResources.get('github');
		img.src = IMAGE_PATH + '/github-logo-white.svg';
	}
	else if (peer == editorUi.gitLab)
	{
		service = mxResources.get('gitlab');
		img.src = IMAGE_PATH + '/gitlab-logo.svg';
	}
	else if (peer == editorUi.trello)
	{
		service = mxResources.get('trello');
		img.src = IMAGE_PATH + '/trello-logo-white.svg';
	}
	
	var p = document.createElement('p');
	mxUtils.write(p, mxResources.get('authorizedIn', [service], 'You are now authorized in {1}'));

	var button = mxUtils.button(btnLbl, fn);

	button.insertBefore(img, button.firstChild);
	button.style.marginTop = '6px';
	button.className = 'geBigButton';
	button.style.fontSize = '18px';
	button.style.padding = '14px';

	div.appendChild(hd);
	div.appendChild(p);
	div.appendChild(button);
	
	this.container = div;
};

/**
 * Constructs a new font dialog.
 */
var FontDialog = function(editorUi, curFontname, curUrl, curType, fn)
{
	var row, td, label;
	
	var table = document.createElement('table');
	var tbody = document.createElement('tbody');
	table.style.marginTop = '8px';

	//System fonts section
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.colSpan = 2;
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.fontWeight = 'bold';
	
	var sysFontRadio = document.createElement('input');
	sysFontRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	sysFontRadio.setAttribute('value', 'sysfonts');
	sysFontRadio.setAttribute('type', 'radio');
	sysFontRadio.setAttribute('name', 'current-fontdialog');
	sysFontRadio.setAttribute('id', 'fontdialog-sysfonts');
	td.appendChild(sysFontRadio);
	
	label = document.createElement('label');
	label.setAttribute('for', 'fontdialog-sysfonts');
	mxUtils.write(label, (mxResources.get('sysFonts', null, 'System Fonts')));
	td.appendChild(label);
	
	row.appendChild(td);
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.width = '120px';
	td.style.paddingLeft = '15px';
	mxUtils.write(td, (mxResources.get('fontname', null, 'Font Name')) + ':');

	row.appendChild(td);
	
	var sysFontInput = document.createElement('input');
	
	if (curType == 's')
	{
		sysFontInput.setAttribute('value', curFontname);
	}
	
	sysFontInput.style.marginLeft = '4px';
	sysFontInput.style.width = '250px';
	sysFontInput.className = 'dlg_fontName_s';
	
	td = document.createElement('td');
	td.appendChild(sysFontInput);
	row.appendChild(td);
	
	tbody.appendChild(row);

	//Google fonts section
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.colSpan = 2;
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.fontWeight = 'bold';
	
	var googleFontRadio = document.createElement('input');
	googleFontRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	googleFontRadio.setAttribute('value', 'googlefonts');
	googleFontRadio.setAttribute('type', 'radio');
	googleFontRadio.setAttribute('name', 'current-fontdialog');
	googleFontRadio.setAttribute('id', 'fontdialog-googlefonts');
	td.appendChild(googleFontRadio);
	
	label = document.createElement('label');
	label.setAttribute('for', 'fontdialog-googlefonts');
	mxUtils.write(label, (mxResources.get('googleFonts', null, 'Google Fonts')));
	td.appendChild(label);
	
	// Link to Google Fonts
	if (!editorUi.isOffline() || EditorUi.isElectronApp)
	{
		var link = editorUi.createHelpIcon('https://fonts.google.com/');
		td.appendChild(link);
	}
	
	row.appendChild(td);

	if (urlParams['isGoogleFontsEnabled'] != '0')
	{
		tbody.appendChild(row);
	}

	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.width = '120px';
	td.style.paddingLeft = '15px';
	mxUtils.write(td, (mxResources.get('fontname', null, 'Font Name')) + ':');

	row.appendChild(td);
	
	var googleFontInput = document.createElement('input');

	if (curType == 'g')
	{
		googleFontInput.setAttribute('value', curFontname);
	}
	
	googleFontInput.style.marginLeft = '4px';
	googleFontInput.style.width = '250px';
	googleFontInput.className = 'dlg_fontName_g';
	
	td = document.createElement('td');
	td.appendChild(googleFontInput);
	row.appendChild(td);

	if (urlParams['isGoogleFontsEnabled'] != '0')
	{
		tbody.appendChild(row);
	}
	
	//Generic remote fonts section
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.colSpan = 2;
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.fontWeight = 'bold';

	var webFontRadio = document.createElement('input');
	webFontRadio.style.cssText = 'margin-right:8px;margin-bottom:8px;';
	webFontRadio.setAttribute('value', 'webfonts');
	webFontRadio.setAttribute('type', 'radio');
	webFontRadio.setAttribute('name', 'current-fontdialog');
	webFontRadio.setAttribute('id', 'fontdialog-webfonts');
	td.appendChild(webFontRadio);
	
	label = document.createElement('label');
	label.setAttribute('for', 'fontdialog-webfonts');
	mxUtils.write(label, (mxResources.get('webfonts', null, 'Web Fonts')));
	td.appendChild(label);
	
	row.appendChild(td);

	if (Editor.enableWebFonts)
	{
		tbody.appendChild(row);
	}
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.width = '120px';
	td.style.paddingLeft = '15px';
	mxUtils.write(td, (mxResources.get('fontname', null, 'Font Name')) + ':');

	row.appendChild(td);
	
	var webFontInput = document.createElement('input');

	if (curType == 'w')
	{
		if (Editor.enableWebFonts)
		{
			webFontInput.setAttribute('value', curFontname);
		}
		else
		{
			sysFontInput.setAttribute('value', curFontname);
		}
	}
	
	webFontInput.style.marginLeft = '4px';
	webFontInput.style.width = '250px';
	webFontInput.className = 'dlg_fontName_w';
	
	td = document.createElement('td');
	td.appendChild(webFontInput);
	row.appendChild(td);

	if (Editor.enableWebFonts)
	{
		tbody.appendChild(row);
	}
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.whiteSpace = 'nowrap';
	td.style.fontSize = '10pt';
	td.style.width = '120px';
	td.style.paddingLeft = '15px';
	mxUtils.write(td, (mxResources.get('fontUrl', null, 'Font URL')) + ':');

	row.appendChild(td);
	
	var webFontUrlInput = document.createElement('input');
	webFontUrlInput.setAttribute('value', curUrl || '');
	webFontUrlInput.style.marginLeft = '4px';
	webFontUrlInput.style.width = '250px';
	webFontUrlInput.className = 'dlg_fontUrl';
	
	td = document.createElement('td');
	td.appendChild(webFontUrlInput);
	row.appendChild(td);

	if (Editor.enableWebFonts)
	{
		tbody.appendChild(row);
	}
	
	this.init = function()
	{
		var input = sysFontInput;
		
		if (curType == 'g')
		{
			input = googleFontInput;
		}
		else if (curType == 'w' && Editor.enableWebFonts)
		{
			input = webFontInput;
		}
		
		input.focus();
		
		if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
		{
			input.select();
		}
		else
		{
			document.execCommand('selectAll', false, null);
		}
	};

	row = document.createElement('tr');
	td = document.createElement('td');
	td.colSpan = 2;
	td.style.paddingTop = '20px';
	td.style.whiteSpace = 'nowrap';
	td.setAttribute('align', 'right');
	
	if (!editorUi.isOffline())
	{
		td.appendChild(editorUi.createHelpIcon('https://www.drawio.com/blog/external-fonts'));
	}
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
		fn();
	});
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		td.appendChild(cancelBtn);
	}
	
	function validateFn(fontName, fontUrl, type)
	{
		var urlPattern = /(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?/;
		var elt = table.querySelector('.dlg_fontName_' + type);
		
		if (elt != null && (fontName == null || fontName.length == 0))
		{
			elt.style.border = '1px solid red';
			return false;
		}

		elt = table.querySelector('.dlg_fontUrl');
		
		if (elt != null && type == 'w' && !urlPattern.test(fontUrl))
		{
			elt.style.border = '1px solid red';
			return false;
		}
		
		return true;
	};
	
	var okBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		var fontName, fontUrl, type;
		
		if (sysFontRadio.checked)
		{
			fontName = sysFontInput.value;
			type = 's';
		}
		else if (googleFontRadio.checked)
		{
			fontName = googleFontInput.value;
			fontUrl = Editor.GOOGLE_FONTS + encodeURIComponent(fontName).replace(/%20/g, '+');
			type = 'g';
		}
		else if (webFontRadio.checked)
		{
			fontName = webFontInput.value;
			fontUrl = webFontUrlInput.value;
			type = 'w';
		}
		
		if (validateFn(fontName, fontUrl, type))
		{
			fn(fontName, fontUrl, type);
			editorUi.hideDialog();
		}
	});
	okBtn.className = 'geBtn gePrimaryBtn';

	function enterSubmit(e)
	{
		this.style.border = '';
		
		if (e.keyCode == 13)
		{
			okBtn.click();
		}
	};
	
	mxEvent.addListener(sysFontInput, 'keypress', enterSubmit);
	mxEvent.addListener(googleFontInput, 'keypress', enterSubmit);
	mxEvent.addListener(webFontInput, 'keypress', enterSubmit);
	mxEvent.addListener(webFontUrlInput, 'keypress', enterSubmit);
	
	mxEvent.addListener(sysFontInput, 'focus', function()
	{
		sysFontRadio.setAttribute('checked', 'checked');
		sysFontRadio.checked = true;
	});
	
	mxEvent.addListener(googleFontInput, 'focus', function()
	{
		googleFontRadio.setAttribute('checked', 'checked');
		googleFontRadio.checked = true;
	});

	mxEvent.addListener(webFontInput, 'focus', function()
	{
		webFontRadio.setAttribute('checked', 'checked');
		webFontRadio.checked = true;
	});

	mxEvent.addListener(webFontUrlInput, 'focus', function()
	{
		webFontRadio.setAttribute('checked', 'checked');
		webFontRadio.checked = true;
	});

	td.appendChild(okBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		td.appendChild(cancelBtn);
	}

	row.appendChild(td);
	tbody.appendChild(row);
	table.appendChild(tbody);
	
	this.container = table;
};

/* Aspect Dialog
 * @module drawio/aspect-dialog
 */
function AspectDialog(editorUi, pageId, layerIds, okFn, cancelFn)
{
	this.aspect = {pageId : pageId || (editorUi.pages? editorUi.pages[0].getId() : null), layerIds : layerIds || []};
	var div = document.createElement('div');
	
	var title = document.createElement('h5');
	title.style.margin = '0 0 10px';
	mxUtils.write(title, mxResources.get('pages'));
	div.appendChild(title);

	var pagesContainer = document.createElement('div');
	pagesContainer.className = 'geAspectDlgList';
	div.appendChild(pagesContainer);

	title = document.createElement('h5');
	title.style.margin = '0 0 10px';
	mxUtils.write(title, mxResources.get('layers'));
	div.appendChild(title);

	var layersContainer = document.createElement('div');
	layersContainer.className = 'geAspectDlgList';
	div.appendChild(layersContainer);
	
	this.pagesContainer = pagesContainer;
	this.layersContainer = layersContainer;
	this.ui = editorUi;
	
	var btns = document.createElement('div');
	btns.style.marginTop = '16px';
	btns.style.textAlign = 'center';
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
		
		if (cancelFn != null)
		{
			cancelFn();
		}
	});
	
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	var okBtn = mxUtils.button(mxResources.get('ok'), mxUtils.bind(this, function()
	{
		editorUi.hideDialog();
		okFn({pageId: this.selectedPage, layerIds: Object.keys(this.selectedLayers)});
	}));

	btns.appendChild(okBtn);
	okBtn.className = 'geBtn gePrimaryBtn';
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	okBtn.setAttribute('disabled', 'disabled');
	this.okBtn = okBtn;
	div.appendChild(btns);
	this.container = div;
};

//Drawing the graph with dialog not visible doesn't get dimensions right. It has to be visible!
AspectDialog.prototype.init = function()
{
	var xml = this.ui.getFileData(true); //Force pages to update their nodes
	
	if (this.ui.pages)
	{
		for (var i = 0; i < this.ui.pages.length; i++)
		{
			var page = this.ui.updatePageRoot(this.ui.pages[i]);
	
			this.createPageItem(page.getId(), page.getName(), page.node);
		}
	}
	else
	{
		this.createPageItem('1', 'Page-1', mxUtils.parseXml(xml).documentElement);
	}
};

AspectDialog.prototype.createViewer = function(container, pageNode, layerId, defaultBackground)
{
	mxEvent.disableContextMenu(container);
	container.style.userSelect = 'none';

	var graph = new Graph(container);
	graph.setTooltips(false);
	graph.setEnabled(false);
	graph.setPanning(false);
	graph.minFitScale = null;
	graph.maxFitScale = null;
	graph.centerZoom = true;
	
	var node = pageNode.nodeName == 'mxGraphModel'? pageNode : Editor.parseDiagramNode(pageNode); //Handles compressed and non-compressed page node
	
	if (node != null)
	{
		var bg = node.getAttribute('background');
		
		if (bg == null || bg == '' || bg == mxConstants.NONE)
		{
			bg = (defaultBackground != null) ? defaultBackground : '#ffffff';
		}
		
		container.style.backgroundColor = bg;
		
		var codec = new mxCodec(node.ownerDocument);
		var model = graph.getModel();
		codec.decode(node, model);
		
		var childCount = model.getChildCount(model.root);
		
		var showAll = layerId == null;
		
		// handle layers visibility
		for (var i = 0; i < childCount; i++)
		{
			var child = model.getChildAt(model.root, i);
			model.setVisible(child, showAll || layerId == child.id);
		}

		graph.maxFitScale = 1;
		graph.fit(0);
		graph.center();
	}
	
	return graph;
};

AspectDialog.prototype.createPageItem = function(pageId, pageName, pageNode)
{
	var $listItem = document.createElement('div');
	$listItem.className = 'geAspectDlgListItem';
	$listItem.setAttribute('data-page-id', pageId)
	$listItem.innerHTML = '<div style="max-width: 100%; max-height: 100%;"></div><div class="geAspectDlgListItemText">' + mxUtils.htmlEntities(pageName) + '</div>';
	
	this.pagesContainer.appendChild($listItem);
	
	var graph = this.createViewer($listItem.childNodes[0], pageNode);
	
	var onClick = mxUtils.bind(this, function()
	{
		if (this.selectedItem != null)
		{
			this.selectedItem.className = 'geAspectDlgListItem';
		}
		
		this.selectedItem = $listItem;
		this.selectedPage = pageId;
		$listItem.className += ' geAspectDlgListItemSelected';
		this.layersContainer.innerText = '';
		this.selectedLayers = {};
		this.okBtn.setAttribute('disabled', 'disabled');
		
		var graphModel = graph.model;
		var layers = graphModel.getChildCells(graphModel.getRoot());
		
		for (var i = 0; i < layers.length; i++) 
		{
			this.createLayerItem(layers[i], pageId, graph, pageNode);
		}
	});
	
	mxEvent.addListener($listItem, 'click', onClick);
	
	if(this.aspect.pageId == pageId) 
	{
		onClick();
	}
};

AspectDialog.prototype.createLayerItem = function(layer, pageId, graph, pageNode)
{
	var layerName = graph.convertValueToString(layer) || (mxResources.get('background') || 'Background');
	var $listItem = document.createElement('div');
	$listItem.setAttribute('data-layer-id', layer.id);
	$listItem.className = 'geAspectDlgListItem';
	$listItem.innerHTML = '<div style="max-width: 100%; max-height: 100%;"></div><div class="geAspectDlgListItemText">' + mxUtils.htmlEntities(layerName) + '</div>';
	this.layersContainer.appendChild($listItem);
	
	this.createViewer($listItem.childNodes[0], pageNode, layer.id);

	var onClick = mxUtils.bind(this, function()
	{
		if ($listItem.className.indexOf('geAspectDlgListItemSelected') >= 0) //Selected
		{
			$listItem.className = 'geAspectDlgListItem';
			delete this.selectedLayers[layer.id];
			
			if (mxUtils.isEmptyObject(this.selectedLayers))
			{
				this.okBtn.setAttribute('disabled', 'disabled');
			}
		}
		else
		{
			$listItem.className += ' geAspectDlgListItemSelected';
			this.selectedLayers[layer.id] = true;
			this.okBtn.removeAttribute('disabled');
		}
	});
	
	mxEvent.addListener($listItem, 'click', onClick);
	
	if(this.aspect.layerIds.indexOf(layer.id) != -1) 
	{
		onClick();
	}
};

/**
 * Constructs a new page setup dialog.
 */
var FilePropertiesDialog = function(editorUi, publicLink)
{	
	var row, td;
	var table = document.createElement('table');
	var tbody = document.createElement('tbody');
	table.style.width = '100%';
	table.style.height = '100%';
	table.style.tableLayout = 'fixed';
	
	var file = editorUi.getCurrentFile();
	var filename = (file != null && file.getTitle() != null) ?
		file.getTitle() : editorUi.defaultFilename;
	var isPng = /(\.png)$/i.test(filename);
	var isSvg = /(\.svg)$/i.test(filename);
	var apply = function(success, error)
	{
		success();
	};

	function addApply(fn)
	{
		var prevApply = apply;

		apply = function(success, error)
		{
			try
			{
				fn(function()
				{
					prevApply(success, error);
				}, error);
			}
			catch (e)
			{
				error(e);
			}
		};
	};
	
	var initialLocked = (file != null) ? file.isLocked() : false;

	row = document.createElement('tr');
	td = document.createElement('td');
	td.style.whiteSpace = 'nowrap';
	td.style.overflow = 'hidden';
	td.style.textOverflow = 'ellipsis';
	td.style.fontSize = '10pt';

	mxUtils.write(td, mxResources.get('locked') + ':');
	row.appendChild(td);

	var lockedInput = document.createElement('input');
	lockedInput.setAttribute('type', 'checkbox');
	
	if (initialLocked)
	{
		lockedInput.setAttribute('checked', 'checked');
		lockedInput.defaultChecked = true;
	}
	
	td = document.createElement('td');
	td.style.whiteSpace = 'nowrap';
	td.appendChild(lockedInput);
	row.appendChild(td);
	tbody.appendChild(row);

	this.init = function()
	{
		lockedInput.focus();
	};

	addApply(function(success, error)
	{
		if (editorUi.fileNode != null && initialLocked != lockedInput.checked)
		{
			window.setTimeout(function()
			{
				if (file != null)
				{
					file.setLocked(lockedInput.checked);
				}

				success();
			}, 0);
		}
		else
		{
			success();
		}
	});

	if (isPng || isSvg)
	{
		var scale = 1;
		var border = 0;
		var node = editorUi.fileNode;
	
		if (node != null)
		{
			if (node.hasAttribute('scale'))
			{
				scale = parseFloat(node.getAttribute('scale'));
			}
			
			if (node.hasAttribute('border'))
			{
				border = parseInt(node.getAttribute('border'));
			}
		}
		
		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('zoom') + ':');
		
		row.appendChild(td);
	
		var zoomInput = document.createElement('input');
		zoomInput.setAttribute('value', (scale * 100) + '%');
		zoomInput.style.boxSizing = 'border-box';
		zoomInput.style.width = '100%';
		
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.appendChild(zoomInput);
		row.appendChild(td);
		tbody.appendChild(row);
		
		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('borderWidth') + ':');
		
		row.appendChild(td);
	
		var borderInput = document.createElement('input');
		borderInput.setAttribute('value', border);
		borderInput.style.boxSizing = 'border-box';
		borderInput.style.width = '100%';
		
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.appendChild(borderInput);
		row.appendChild(td);
		tbody.appendChild(row);
		
		this.init = this.init || function()
		{
			zoomInput.focus();
			
			if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
			{
				zoomInput.select();
			}
			else
			{
				document.execCommand('selectAll', false, null);
			}
		};

		addApply(function(success, error)
		{
			if (editorUi.fileNode != null)
			{
				editorUi.fileNode.setAttribute('scale', Math.max(0, parseInt(zoomInput.value) / 100));
				editorUi.fileNode.setAttribute('border', Math.max(0, parseInt(borderInput.value)));
				
				if (file != null)
				{
					file.fileChanged();
				}
			}

			success();
		});
	}
	else if (!/(\.html)$/i.test(filename) &&
		!/(\.svg)$/i.test(filename))
	{
		var initialCompressed = (file != null) ? file.isCompressed() : Editor.defaultCompressed;

		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.overflow = 'hidden';
		td.style.textOverflow = 'ellipsis';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('compressed') + ':');
		
		row.appendChild(td);
	
		var compressedInput = document.createElement('input');
		compressedInput.setAttribute('type', 'checkbox');
		
		if (initialCompressed)
		{
			compressedInput.setAttribute('checked', 'checked');
			compressedInput.defaultChecked = true;
		}

		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.appendChild(compressedInput);
		row.appendChild(td);
		tbody.appendChild(row);
		
		this.init = this.init || function()
		{
			compressedInput.focus();
		};

		addApply(function(success, error)
		{
			if (editorUi.fileNode != null && initialCompressed != compressedInput.checked)
			{
				window.setTimeout(function()
				{
					editorUi.fileNode.setAttribute('compressed',
						(compressedInput.checked) ? 'true' : 'false');
					
					if (file != null)
					{
						file.compressionChanged(compressedInput.checked);
						file.fileChanged();
					}

					success();
				}, 0);
			}
			else
			{
				success();
			}
		});
	}
	
	if (file != null && file.isRealtimeOptional())
	{
		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.overflow = 'hidden';
		td.style.textOverflow = 'ellipsis';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('realtimeCollaboration') + ':');
		row.appendChild(td);
	
		var collabInput = document.createElement('input');
		collabInput.setAttribute('type', 'checkbox');
		var initialCollab = file.isRealtimeEnabled();

		var collab = editorUi.drive.getCustomProperty(file.desc, 'collaboration');
		var initialCollab = collab != 'disabled';
	
		if (initialCollab)
		{
			collabInput.setAttribute('checked', 'checked');
			collabInput.defaultChecked = true;
		}

		addApply(function(success, error)
		{
			if (collabInput.checked != initialCollab)
			{
				file.setRealtimeEnabled(collabInput.checked, success, error);
			}
			else
			{
				success();
			}
		});

		this.init = (this.init != null) ? this.init : function()
		{
			collabInput.focus();
		};

		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		
		var div = document.createElement('div');
		div.style.display = 'flex';
		div.style.alignItems = 'center';
		div.style.justifyContent = 'start';
		div.appendChild(collabInput);
		div.appendChild(editorUi.menus.createHelpLink('https://github.com/jgraph/drawio/discussions/2672'));
		td.appendChild(div);
		row.appendChild(td);
		tbody.appendChild(row);
	}

	if (file != null && editorUi.getServiceName() == 'draw.io' &&
		file.getSize() > 0 && urlParams['embed'] != '1')
	{
		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.overflow = 'hidden';
		td.style.textOverflow = 'ellipsis';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('size') + ':');
		row.appendChild(td);

		var temp = editorUi.formatFileSize(file.getSize());

		var sizeInput = document.createElement('input');
		sizeInput.setAttribute('title', temp);
		sizeInput.setAttribute('value', temp);
		sizeInput.setAttribute('disabled', 'disabled');
		sizeInput.style.boxSizing = 'border-box';
		sizeInput.style.width = '100%';

		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.appendChild(sizeInput);
		row.appendChild(td);
		tbody.appendChild(row);
	}

	if (publicLink != null)
	{
		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.overflow = 'hidden';
		td.style.textOverflow = 'ellipsis';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('publicDiagramUrl') + ':');
		row.appendChild(td);

		var a = document.createElement('a');
		a.setAttribute('href', publicLink);
		a.setAttribute('title', publicLink);
		a.style.whiteSpace = 'nowrap';
		a.style.overflow = 'hidden';
		a.style.textOverflow = 'ellipsis';
		a.style.display = 'block';
		a.style.margin = '2px';
		a.style.fontSize = '10pt';
		mxUtils.write(a, publicLink);

		mxEvent.addListener(a, 'click', function(evt)
		{
			editorUi.openLink(publicLink);
			mxEvent.consume(evt);
		});

		td = document.createElement('td');
		td.appendChild(a);
		row.appendChild(td);
		tbody.appendChild(row);
	}

	if (file != null && file.fileObject != null &&
		file.fileObject.path != null)
	{
		row = document.createElement('tr');
		td = document.createElement('td');
		td.style.whiteSpace = 'nowrap';
		td.style.overflow = 'hidden';
		td.style.textOverflow = 'ellipsis';
		td.style.fontSize = '10pt';
		mxUtils.write(td, mxResources.get('pathFilename') + ':');
		row.appendChild(td);

		var pathInput = document.createElement('input');
		pathInput.setAttribute('title', file.fileObject.path);
		pathInput.setAttribute('value', file.fileObject.path);
		pathInput.setAttribute('disabled', 'disabled');
		pathInput.style.boxSizing = 'border-box';
		pathInput.style.width = '100%';

		td = document.createElement('td');
		td.appendChild(pathInput);
		row.appendChild(td);
		tbody.appendChild(row);
	}

	this.init = (this.init != null) ? this.init : function() { };

	var genericBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		if (editorUi.spinner.spin(document.body, mxResources.get('updatingDocument')))
		{
			apply(function()
			{
				editorUi.spinner.stop();
				editorUi.hideDialog();
			}, function(e)
			{
				editorUi.spinner.stop();
				editorUi.handleError(e, mxResources.get('error'));
			});
		}
	});
	genericBtn.className = 'geBtn gePrimaryBtn';
	
	row = document.createElement('tr');
	td = document.createElement('td');
	td.colSpan = 2;
	td.style.paddingTop = '20px';
	td.style.whiteSpace = 'nowrap';
	td.setAttribute('align', 'right');
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		td.appendChild(cancelBtn);
	}


	if (editorUi.fileNode != null)
	{
		var varsBtn = mxUtils.button(mxResources.get('editData') + '...', function()
		{
			editorUi.hideDialog();

			// Parse current vars from fileNode
			var vars = {};

			try
			{
				var varsStr = editorUi.fileNode.getAttribute('vars');

				if (varsStr != null && varsStr.length > 0)
				{
					vars = JSON.parse(varsStr);
				}
			}
			catch (e) {}

			// Create temp XML node with vars as attributes
			var doc = mxUtils.createXmlDocument();
			var obj = doc.createElement('object');
			obj.setAttribute('label', '');

			for (var key in vars)
			{
				obj.setAttribute(key, vars[key]);
			}

			// Use a temporary graph so EditDataDialog operates on
			// its own model instead of overriding setValue on the
			// real graph model.
			var tempGraph = editorUi.createTemporaryGraph(
				editorUi.editor.graph.getStylesheet());
			var tempCell = new mxCell(obj);
			tempGraph.getModel().add(tempGraph.getDefaultParent(), tempCell);

			tempGraph.getModel().addListener(mxEvent.CHANGE, function(sender, evt)
			{
				var changes = evt.getProperty('edit').changes;

				for (var i = 0; i < changes.length; i++)
				{
					if (changes[i] instanceof mxValueChange &&
						changes[i].cell === tempCell)
					{
						var value = changes[i].value;
						var newVars = {};
						var attrs = value.attributes;

						for (var j = 0; j < attrs.length; j++)
						{
							if (attrs[j].nodeName != 'label')
							{
								newVars[attrs[j].nodeName] = attrs[j].nodeValue;
							}
						}

						var json = JSON.stringify(newVars);

						if (json == '{}')
						{
							editorUi.fileNode.removeAttribute('vars');
						}
						else
						{
							editorUi.fileNode.setAttribute('vars', json);
						}

						editorUi.updateFileVars();
						editorUi.editor.graph.refresh();

						var currentFile = editorUi.getCurrentFile();

						if (currentFile != null)
						{
							currentFile.fileChanged();
						}
					}
				}
			});

			// Open EditDataDialog with temp graph
			var dlg = new EditDataDialog(editorUi, tempCell, tempGraph);
			editorUi.showDialog(dlg.container, 480, 420, true, false, null,
				false, null, new mxRectangle(0, 0, 440, 220));
			dlg.init();
		});

		varsBtn.className = 'geBtn';
		td.appendChild(varsBtn);
	}

	td.appendChild(genericBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		td.appendChild(cancelBtn);
	}

	row.appendChild(td);
	tbody.appendChild(row);
	table.appendChild(tbody);
	
	this.container = table;
};

var ConnectionPointsDialog = function(editorUi, cell)
{
	var CP_SIZE = 6, CP_HLF_SIZE = 3;
	var div = document.createElement('div');
	div.style.userSelect = 'none';
	div.style.display = 'flex';
	div.style.flexDirection = 'column';
	div.style.height = '100%';
	var keyHandler = null;
	var resizeObserver = null;

	this.init = function()
	{
		var graphDiv = document.createElement('div');
		graphDiv.style.flex = '1';
		graphDiv.style.minHeight = '0';
		graphDiv.style.overflow = 'hidden';
		graphDiv.style.borderStyle = 'solid';
		graphDiv.style.borderWidth = '1px';
		graphDiv.style.boxSizing = 'border-box';
		mxEvent.disableContextMenu(graphDiv);
		div.appendChild(graphDiv);

		var editingGraph = new Graph(graphDiv);
		editingGraph.transparentBackground = false;
		editingGraph.autoExtend = false;
		editingGraph.autoScroll = false;
		editingGraph.setGridEnabled(false);
		editingGraph.setEnabled(true);
		editingGraph.setPanning(true);
		editingGraph.setConnectable(false);
		editingGraph.setTooltips(false);
		editingGraph.minFitScale = null;
		editingGraph.maxFitScale = null;
		editingGraph.centerZoom = true;
		editingGraph.maxFitScale = 2;

		function createCPoint(x, y, constObj)
		{
			var cPointStyle = 'shape=image;points=[];rotatable=0;resizable=0;connectable=0;editable=0;image=' +
				editorUi.convertDataUri(mxConstraintHandler.prototype.pointImage.src) + ';';
			var cPoint = new mxCell('', new mxGeometry(x, y, CP_SIZE, CP_SIZE), cPointStyle);
			cPoint.vertex = true;
			cPoint.cp = true;
			cPoint.constObj = constObj;

			return editingGraph.addCell(cPoint);
		};
	
		// Add cell and current connection points on it
		var geo = cell.geometry;
		var mainCell = new mxCell(cell.value, new mxGeometry(0, 0, geo.width, geo.height),
			cell.style + ';rotatable=0;resizable=0;connectable=0;editable=0;movable=0;opacity=50;');
		mainCell.vertex = true;
		editingGraph.addCell(mainCell);

		// Adding a point via double click
		editingGraph.dblClick = function(evt, cell)
		{
			if (cell != null && cell != mainCell)
			{
				editingGraph.setSelectionCell(cell);
			}
			else
			{
				var pt = mxUtils.convertPoint(editingGraph.container, mxEvent.getClientX(evt), mxEvent.getClientY(evt));
				mxEvent.consume(evt);
				var scale = editingGraph.view.scale;
				var tr = editingGraph.view.translate;
				editingGraph.setSelectionCell(createCPoint((pt.x - CP_HLF_SIZE * scale) / scale - tr.x,
					(pt.y - CP_HLF_SIZE * scale) / scale - tr.y));
			}
		}

		keyHandler = new mxKeyHandler(editingGraph);
		
		function removeCPoints(evt)
		{
			var cells = editingGraph.getSelectionCells();
			editingGraph.deleteCells(cells);
		};

		keyHandler.bindKey(46, removeCPoints);
		keyHandler.bindKey(8, removeCPoints);

		// Force rubberband inside the cell
		editingGraph.getRubberband().isForceRubberbandEvent = function(event)
		{
			// Left click and not a click on a connection point
			return event.evt.button == 0 &&
				(event.getCell() == null ||
				event.getCell() == mainCell);
		};
		// Force panning inside the cell
		editingGraph.panningHandler.isForcePanningEvent = function(event)
		{
			return event.evt.button == 2;
		};

		var origIsCellSelectable = editingGraph.isCellSelectable;
		editingGraph.isCellSelectable = function(cell)
		{
			if (cell == mainCell)
			{
				return false;
			}
			else
			{
				return origIsCellSelectable.apply(this, arguments);
			}
		};

		// Disables hyperlinks
		editingGraph.getLinkForCell = function()
		{
			return null;
		};

		var state = editingGraph.view.getState(mainCell);
		var constraints = editingGraph.getAllConnectionConstraints(state);
		
		for (var i = 0; constraints != null && i < constraints.length; i++)
		{
			var cp = editingGraph.getConnectionPoint(state, constraints[i]);
			createCPoint(cp.x - CP_HLF_SIZE, cp.y - CP_HLF_SIZE, constraints[i]);
		}

		var zoomInBtn = editorUi.createToolbarButton(Editor.zoomInImage,
			mxResources.get('zoomIn'), function()
		{
			editingGraph.zoomIn();
		});
	
		var zoomOutBtn = editorUi.createToolbarButton(Editor.zoomOutImage,
			mxResources.get('zoomOut'), function()
		{
			editingGraph.zoomOut();
		});
	
		var zoomFitBtn = editorUi.createToolbarButton(Editor.zoomFitImage,
			mxResources.get('fit'), function()
		{
			if (editingGraph.view.scale == 1)
			{
				editingGraph.maxFitScale = 8;
				editingGraph.fit(8);
			}
			else
			{
				editingGraph.zoomActual();
			}

			editingGraph.center();
		});

		var changeGridSize = function()
		{
			editorUi.prompt(mxResources.get('gridSize'), editingGraph.gridSize, function(newValue)
			{
				if (!isNaN(newValue) && newValue > 0)
				{
					editingGraph.setGridSize(newValue);
					editingGraph.setGridEnabled(true);
					editingGraph.refresh();
				}
			});
		};

		var gridBtn = editorUi.createToolbarButton(Editor.thinGridImage,
			mxResources.get('grid'), function(evt)
		{
			if (mxEvent.isShiftDown(evt))
			{
				changeGridSize();
			}
			else
			{
				editingGraph.setGridEnabled(!editingGraph.isGridEnabled());
				editingGraph.refresh();
			}
		});

		mxEvent.addListener(gridBtn, 'dblclick', changeGridSize);

		var deleteBtn = editorUi.createToolbarButton(Editor.trashImage,
			mxResources.get('delete'), removeCPoints);
		mxUtils.setOpacity(deleteBtn, 10); //Disabled
		
		var zoomBtns = document.createElement('div');
		zoomBtns.style.display = 'flex';
		zoomBtns.style.flexShrink = '0';
		zoomBtns.style.alignItems = 'center';
		zoomBtns.style.paddingTop = '6px';

		zoomBtns.appendChild(zoomInBtn);
		zoomBtns.appendChild(zoomOutBtn);
		zoomBtns.appendChild(zoomFitBtn);
		zoomBtns.appendChild(gridBtn);
		zoomBtns.appendChild(deleteBtn);

		div.appendChild(zoomBtns);

		var pCount = document.createElement('input');
		pCount.setAttribute('type', 'number');
		pCount.setAttribute('min', '1');
		pCount.setAttribute('value', '1');
		pCount.style.width = '45px';
		pCount.style.position = 'relative';
		pCount.style.margin = '0 4px 0 4px';

		var sideSelect = document.createElement('select');
		sideSelect.style.position = 'relative';
		var sides = ['left', 'right', 'top', 'bottom'];

		for (var i = 0; i < sides.length; i++)
		{
			var side = sides[i];
			var option = document.createElement('option');
			mxUtils.write(option, mxResources.get(side));
			option.value = side;
			sideSelect.appendChild(option);
		}

		var addBtn = mxUtils.button(mxResources.get('add'), function()
		{
			var count = parseInt(pCount.value);
			count = count < 1? 1 : (count > 100? 100 : count);
			pCount.value = count;
			var side = sideSelect.value;
			var geo = mainCell.geometry;
			var cells = [];

			for (var i = 0; i < count; i++)
			{
				var x, y;

				switch(side)
				{
					case 'left':
						x = geo.x;
						y = geo.y + (i + 1) * geo.height / (count + 1);
						break;
					case 'right':
						x = geo.x + geo.width;
						y = geo.y + (i + 1) * geo.height / (count + 1);
						break;
					case 'top':
						x = geo.x + (i + 1) * geo.width / (count + 1);
						y = geo.y;
						break;
					case 'bottom':
						x = geo.x + (i + 1) * geo.width / (count + 1);
						y = geo.y + geo.height;
						break;
				}

				cells.push(createCPoint(x - CP_HLF_SIZE, y - CP_HLF_SIZE));
			}

			editingGraph.setSelectionCells(cells);
		});

		addBtn.style.marginLeft = 'auto';
		zoomBtns.appendChild(addBtn);
		zoomBtns.appendChild(pCount);
		zoomBtns.appendChild(sideSelect);
		
		//Point properties
		var pointPropsDiv = document.createElement('div');
		pointPropsDiv.style.flexShrink = '0';
		pointPropsDiv.style.margin = '4px 0px 8px 0px';
		pointPropsDiv.style.whiteSpace = 'nowrap';
		pointPropsDiv.style.height = '24px';
		var xSpan = document.createElement('span');
		mxUtils.write(xSpan, mxResources.get('dx'));
		pointPropsDiv.appendChild(xSpan);
		var xInput = document.createElement('input');
		xInput.setAttribute('type', 'number');
		xInput.setAttribute('min', '0');
		xInput.setAttribute('max', '100');
		xInput.style.width = '45px';
		xInput.style.margin = '0 4px 0 4px';
		pointPropsDiv.appendChild(xInput);
		mxUtils.write(pointPropsDiv, '%');

		var dxInput = document.createElement('input');
		dxInput.setAttribute('type', 'number');
		dxInput.style.width = '45px';
		dxInput.style.margin = '0 4px 0 4px';
		pointPropsDiv.appendChild(dxInput);
		mxUtils.write(pointPropsDiv, 'pt');

		var ySpan = document.createElement('span');
		mxUtils.write(ySpan, mxResources.get('dy'));
		ySpan.style.marginLeft = '12px';
		pointPropsDiv.appendChild(ySpan);
		var yInput = document.createElement('input');
		yInput.setAttribute('type', 'number');
		yInput.setAttribute('min', '0');
		yInput.setAttribute('max', '100');
		yInput.style.width = '45px';
		yInput.style.margin = '0 4px 0 4px';
		pointPropsDiv.appendChild(yInput);
		mxUtils.write(pointPropsDiv, '%');

		var dyInput = document.createElement('input');
		dyInput.setAttribute('type', 'number');
		dyInput.style.width = '45px';
		dyInput.style.margin = '0 4px 0 4px';
		pointPropsDiv.appendChild(dyInput);
		mxUtils.write(pointPropsDiv, 'pt');
		div.appendChild(pointPropsDiv);

		function applyPointProp()
		{
			var x = parseInt(xInput.value) || 0;
			x = x < 0? 0 : (x > 100? 100 : x);
			xInput.value = x;

			var y = parseInt(yInput.value) || 0;
			y = y < 0? 0 : (y > 100? 100 : y);
			yInput.value = y;

			var dx = parseInt(dxInput.value) || 0;
			var dy = parseInt(dyInput.value) || 0;
			var constObj = new mxConnectionConstraint(new mxPoint(x/100, y/100), false, null, dx, dy);
			var cp = editingGraph.getConnectionPoint(state, constObj);

			var cell = editingGraph.getSelectionCell();

			if (cell != null)
			{
				cell.constObj = constObj;
				var geo = cell.geometry.clone();
				var scale = editingGraph.view.scale;
				var tr = editingGraph.view.translate;
				geo.x = (cp.x - CP_HLF_SIZE * scale) / scale - tr.x;
				geo.y = (cp.y - CP_HLF_SIZE * scale) / scale - tr.y;
				editingGraph.model.setGeometry(cell, geo);
			}
		};

		function getConstraintFromCPoint(cp)
		{
			if (cp.constObj)
			{
				return {x: cp.constObj.point.x, y: cp.constObj.point.y, dx: cp.constObj.dx, dy: cp.constObj.dy};
			}

			var dx = 0, dy = 0, mGeo = mainCell.geometry;
			var x = mxUtils.format((cp.geometry.x + CP_HLF_SIZE - mGeo.x) / mGeo.width);
			var y = mxUtils.format((cp.geometry.y + CP_HLF_SIZE - mGeo.y) / mGeo.height);

			if (x < 0)
			{
				dx = x * mGeo.width;
				x = 0;
			}
			else if (x > 1)
			{
				dx = (x - 1) * mGeo.width;
				x = 1;
			}

			if (y < 0)
			{
				dy = y * mGeo.height;
				y = 0;
			}
			else if (y > 1)
			{
				dy = (y - 1) * mGeo.height;
				y = 1;
			}

			return {x: x, y: y, dx: parseInt(dx), dy: parseInt(dy)};
		};

		function fillCPointProp(evt)
		{
			if (editingGraph.getSelectionCount() == 1)
			{
				var cell = editingGraph.getSelectionCell();

				// On move events, exact constraint is lost
				if (evt)
				{
					cell.constObj = null;
				}
				
				var constraint = getConstraintFromCPoint(cell);
				xInput.value = constraint.x * 100;
				yInput.value = constraint.y * 100;
				dxInput.value = constraint.dx;
				dyInput.value = constraint.dy;
				pointPropsDiv.style.visibility = '';
			}
			else
			{
				pointPropsDiv.style.visibility = 'hidden';
			}
		};

		fillCPointProp();

		editingGraph.getSelectionModel().addListener(mxEvent.CHANGE, function()
		{
			if (editingGraph.getSelectionCount() > 0)
			{
				mxUtils.setOpacity(deleteBtn, 60); //Enabled
			}
			else
			{
				mxUtils.setOpacity(deleteBtn, 10); //Disabled
			}

			fillCPointProp();
		}); 
		editingGraph.addListener(mxEvent.CELLS_MOVED, fillCPointProp);

		mxEvent.addListener(xInput, 'change', applyPointProp);
		mxEvent.addListener(yInput, 'change', applyPointProp);
		mxEvent.addListener(dxInput, 'change', applyPointProp);
		mxEvent.addListener(dyInput, 'change', applyPointProp);

		var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
		{
			destroy();
			editorUi.hideDialog();
		});

		cancelBtn.className = 'geBtn';
		
		var applyBtn = mxUtils.button(mxResources.get('apply'), function()
		{
			var cells = editingGraph.model.cells, points = [], constraints = [];

			for (var id in cells)
			{
				var cp = cells[id];

				if (!cp.cp) continue;

				constraints.push(getConstraintFromCPoint(cp));
			}

			//Find and remove identical points
			constraints.sort(function(a, b) 
			{
				return (a.x != b.x) ? a.x - b.x : ((a.y != b.y) ? a.y - b.y : 
						((a.dx != b.dx) ? a.dx - b.dx : a.dy - b.dy)); //Sort based on x then y, dx and dy
			});

			for (var i = 0; i < constraints.length; i++)
			{
				if (i > 0 && constraints[i].x == constraints[i - 1].x && constraints[i].y == constraints[i - 1].y 
						  && constraints[i].dx == constraints[i - 1].dx && constraints[i].dy == constraints[i - 1].dy)
				{
					continue; //Skip this identical point
				}

				points.push('[' + constraints[i].x + ',' + constraints[i].y + ',0,' + 
					constraints[i].dx + ',' + constraints[i].dy + ']');
			}

			editorUi.editor.graph.setCellStyles('points', '[' + points.join(',') + ']', [cell]);
			destroy();
			editorUi.hideDialog();
		});
		
		applyBtn.className = 'geBtn gePrimaryBtn';
		
		var resetBtn = mxUtils.button(mxResources.get('reset'), function()
		{
			editorUi.editor.graph.setCellStyles('points', null, [cell]);
			destroy();
			editorUi.hideDialog();
		});
		
		resetBtn.className = 'geBtn';
		
		var buttons = document.createElement('div');
		buttons.style.flexShrink = '0';
		buttons.style.marginTop = '10px';
		buttons.style.textAlign = 'right';

		if (!editorUi.isOffline())
		{
			buttons.appendChild(editorUi.createHelpIcon(
				'https://www.drawio.com/doc/faq/shape-connection-points-customise'));
		}

		if (editorUi.editor.cancelFirst)
		{
			buttons.appendChild(cancelBtn);
		}
		
		buttons.appendChild(resetBtn);
		buttons.appendChild(applyBtn);

		if (!editorUi.editor.cancelFirst)
		{
			buttons.appendChild(cancelBtn);
		}

		div.appendChild(buttons);

		editingGraph.fit(8);
		editingGraph.center();
	};

	function destroy()
	{
		if (keyHandler != null)
		{
			keyHandler.destroy();
		}
	};

	this.destroy = destroy;

	this.container = div;
};

/**
 * Constructs a new polygon editing dialog for mxgraph.basic.polygon shapes.
 */
var PolygonDialog = function(editorUi, cell)
{
	var graph = editorUi.editor.graph;
	var CANVAS_SIZE = 400;
	var VERTEX_RADIUS = 5;
	var SNAP_SIZE = 20;

	var points = [];
	var selectedIndex = -1;
	var closePath = true;
	var snapToGrid = true;
	var undoStack = [];
	var redoStack = [];
	var dragIndex = -1;
	var dragType = null;
	var isDragging = false;
	var dragStarted = false;

	var viewX = 0, viewY = 0;
	var viewW = CANVAS_SIZE, viewH = CANVAS_SIZE;
	var zoom = 1;
	var isPanning = false;
	var panStartX = 0, panStartY = 0;
	var panStartViewX = 0, panStartViewY = 0;
	var spacePressed = false;
	var MIN_ZOOM = 0.05;
	var MAX_ZOOM = 5;

	var listDragSource = null;

	// Load current polygon data from cell style
	var state = graph.view.getState(cell);

	if (state != null)
	{
		try
		{
			var coords = JSON.parse(mxUtils.getValue(state.style, 'polyCoords', '[]'));
			var curves = JSON.parse(mxUtils.getValue(state.style, 'polyCurves', '[]'));
			var polyline = mxUtils.getValue(state.style, 'polyline', 0);
			closePath = !(polyline == 1 || polyline === true ||
				polyline === 'true' || polyline === '1');

			for (var i = 0; i < coords.length; i++)
			{
				var pt = {x: Math.round(coords[i][0] * CANVAS_SIZE),
					y: Math.round(coords[i][1] * CANVAS_SIZE), type: 'L'};

				if (i > 0 && curves.length > i - 1 && curves[i - 1] != null &&
					curves[i - 1].length >= 3 && curves[i - 1][0] === 'Q')
				{
					pt.type = 'Q';
					pt.cx = Math.round(curves[i - 1][1] * CANVAS_SIZE);
					pt.cy = Math.round(curves[i - 1][2] * CANVAS_SIZE);
				}

				points.push(pt);
			}

			// Check closing segment curve
			if (closePath && coords.length > 2 && curves.length >= coords.length &&
				curves[coords.length - 1] != null && curves[coords.length - 1].length >= 3 &&
				curves[coords.length - 1][0] === 'Q')
			{
				points[0].type = 'Q';
				points[0].cx = Math.round(curves[coords.length - 1][1] * CANVAS_SIZE);
				points[0].cy = Math.round(curves[coords.length - 1][2] * CANVAS_SIZE);
			}
		}
		catch (e)
		{
			// ignore
		}
	}

	var div = document.createElement('div');
	div.style.userSelect = 'none';
	div.setAttribute('tabindex', '0');
	div.style.outline = 'none';
	div.style.position = 'absolute';
	div.style.left = '30px';
	div.style.right = '30px';
	div.style.top = '30px';
	div.style.bottom = '30px';

	// Main content area - flex layout
	var contentDiv = document.createElement('div');
	contentDiv.style.display = 'flex';
	contentDiv.style.gap = '10px';
	contentDiv.style.position = 'absolute';
	contentDiv.style.left = '0px';
	contentDiv.style.right = '0px';
	contentDiv.style.top = '0px';
	contentDiv.style.bottom = '40px';
	div.appendChild(contentDiv);

	// Left: SVG canvas
	var svgContainer = document.createElement('div');
	svgContainer.style.border = '1px solid';
	svgContainer.style.borderColor = 'inherit';
	svgContainer.style.borderRadius = '4px';
	svgContainer.style.flexGrow = '1';
	svgContainer.style.flexShrink = '1';
	svgContainer.style.minWidth = '200px';
	svgContainer.style.position = 'relative';
	svgContainer.style.overflow = 'hidden';
	contentDiv.appendChild(svgContainer);

	var svgNS = 'http://www.w3.org/2000/svg';
	var svg = document.createElementNS(svgNS, 'svg');
	svg.setAttribute('width', '100%');
	svg.setAttribute('height', '100%');
	svg.setAttribute('viewBox', '0 0 ' + CANVAS_SIZE + ' ' + CANVAS_SIZE);
	svg.style.display = 'block';
	svg.style.cursor = 'crosshair';

	// Grid pattern
	var defs = document.createElementNS(svgNS, 'defs');
	var pattern = document.createElementNS(svgNS, 'pattern');
	var gridPatternId = 'polygonGrid_' + Date.now();
	pattern.setAttribute('id', gridPatternId);
	pattern.setAttribute('width', SNAP_SIZE);
	pattern.setAttribute('height', SNAP_SIZE);
	pattern.setAttribute('patternUnits', 'userSpaceOnUse');

	var gridPath = document.createElementNS(svgNS, 'path');
	gridPath.setAttribute('d', 'M ' + SNAP_SIZE + ' 0 L 0 0 0 ' + SNAP_SIZE);
	gridPath.setAttribute('fill', 'none');
	gridPath.setAttribute('stroke', '#e0e0e0');
	gridPath.setAttribute('stroke-width', '0.5');
	pattern.appendChild(gridPath);
	defs.appendChild(pattern);
	svg.appendChild(defs);

	var gridRect = document.createElementNS(svgNS, 'rect');
	gridRect.setAttribute('x', '-5000');
	gridRect.setAttribute('y', '-5000');
	gridRect.setAttribute('width', '10000');
	gridRect.setAttribute('height', '10000');
	gridRect.setAttribute('fill', 'url(#' + gridPatternId + ')');
	svg.appendChild(gridRect);

	// Polygon path element
	var polyPath = document.createElementNS(svgNS, 'path');
	polyPath.setAttribute('fill', 'rgba(66, 133, 244, 0.1)');
	polyPath.setAttribute('stroke', '#4285f4');
	polyPath.setAttribute('stroke-width', '2');
	svg.appendChild(polyPath);

	// Group for control point guide lines
	var controlGuideGroup = document.createElementNS(svgNS, 'g');
	svg.appendChild(controlGuideGroup);

	// Group for vertex circles
	var vertexGroup = document.createElementNS(svgNS, 'g');
	svg.appendChild(vertexGroup);

	// Group for control point handles
	var controlPointGroup = document.createElementNS(svgNS, 'g');
	svg.appendChild(controlPointGroup);

	svgContainer.appendChild(svg);

	// Scrollbar constants
	var SCROLLBAR_SIZE = 12;
	var SCROLL_MIN = -CANVAS_SIZE;
	var SCROLL_MAX = 2 * CANVAS_SIZE;
	var SCROLL_RANGE = SCROLL_MAX - SCROLL_MIN;

	// Horizontal scrollbar
	var hScrollbar = document.createElement('div');
	hScrollbar.style.position = 'absolute';
	hScrollbar.style.bottom = '0px';
	hScrollbar.style.left = '0px';
	hScrollbar.style.right = SCROLLBAR_SIZE + 'px';
	hScrollbar.style.height = SCROLLBAR_SIZE + 'px';
	hScrollbar.style.backgroundColor = 'rgba(128,128,128,0.08)';

	var hThumb = document.createElement('div');
	hThumb.style.position = 'absolute';
	hThumb.style.top = '2px';
	hThumb.style.height = (SCROLLBAR_SIZE - 4) + 'px';
	hThumb.style.borderRadius = '4px';
	hThumb.style.backgroundColor = 'rgba(128,128,128,0.35)';
	hThumb.style.cursor = 'pointer';
	hThumb.style.minWidth = '20px';
	hScrollbar.appendChild(hThumb);
	svgContainer.appendChild(hScrollbar);

	// Vertical scrollbar
	var vScrollbar = document.createElement('div');
	vScrollbar.style.position = 'absolute';
	vScrollbar.style.top = '0px';
	vScrollbar.style.right = '0px';
	vScrollbar.style.bottom = SCROLLBAR_SIZE + 'px';
	vScrollbar.style.width = SCROLLBAR_SIZE + 'px';
	vScrollbar.style.backgroundColor = 'rgba(128,128,128,0.08)';

	var vThumb = document.createElement('div');
	vThumb.style.position = 'absolute';
	vThumb.style.left = '2px';
	vThumb.style.width = (SCROLLBAR_SIZE - 4) + 'px';
	vThumb.style.borderRadius = '4px';
	vThumb.style.backgroundColor = 'rgba(128,128,128,0.35)';
	vThumb.style.cursor = 'pointer';
	vThumb.style.minHeight = '20px';
	vScrollbar.appendChild(vThumb);
	svgContainer.appendChild(vScrollbar);

	function zoomTo(newZoom)
	{
		var rect = svg.getBoundingClientRect();

		if (rect.width > 0 && rect.height > 0)
		{
			var cx = viewX + viewW / 2;
			var cy = viewY + viewH / 2;
			zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
			viewW = rect.width * zoom;
			viewH = rect.height * zoom;
			viewX = cx - viewW / 2;
			viewY = cy - viewH / 2;
			updateViewBox();
		}
	};

	// Scrollbar state
	var hScrollDragging = false;
	var vScrollDragging = false;
	var scrollDragStart = 0;
	var scrollDragStartView = 0;

	function updateScrollbars()
	{
		var containerRect = svgContainer.getBoundingClientRect();

		if (containerRect.width <= 0 || containerRect.height <= 0)
		{
			return;
		}

		// Horizontal
		var trackW = containerRect.width - SCROLLBAR_SIZE;
		var thumbRatio = Math.min(1, viewW / SCROLL_RANGE);
		var thumbW = Math.max(20, trackW * thumbRatio);
		var maxScroll = SCROLL_RANGE - viewW;
		var scrollFrac = maxScroll > 0 ? (viewX - SCROLL_MIN) / maxScroll : 0.5;
		scrollFrac = Math.max(0, Math.min(1, scrollFrac));
		hThumb.style.width = thumbW + 'px';
		hThumb.style.left = (scrollFrac * (trackW - thumbW)) + 'px';

		// Vertical
		var trackH = containerRect.height - SCROLLBAR_SIZE;
		var thumbRatioV = Math.min(1, viewH / SCROLL_RANGE);
		var thumbH = Math.max(20, trackH * thumbRatioV);
		var maxScrollV = SCROLL_RANGE - viewH;
		var scrollFracV = maxScrollV > 0 ? (viewY - SCROLL_MIN) / maxScrollV : 0.5;
		scrollFracV = Math.max(0, Math.min(1, scrollFracV));
		vThumb.style.height = thumbH + 'px';
		vThumb.style.top = (scrollFracV * (trackH - thumbH)) + 'px';
	};

	// Scrollbar thumb drag handlers
	mxEvent.addListener(hThumb, 'mousedown', function(evt)
	{
		hScrollDragging = true;
		scrollDragStart = evt.clientX;
		scrollDragStartView = viewX;
		evt.preventDefault();
		evt.stopPropagation();
	});

	mxEvent.addListener(vThumb, 'mousedown', function(evt)
	{
		vScrollDragging = true;
		scrollDragStart = evt.clientY;
		scrollDragStartView = viewY;
		evt.preventDefault();
		evt.stopPropagation();
	});

	// Scrollbar track click handlers
	mxEvent.addListener(hScrollbar, 'mousedown', function(evt)
	{
		if (evt.target === hScrollbar)
		{
			var trackW = hScrollbar.getBoundingClientRect().width;
			var clickX = evt.clientX - hScrollbar.getBoundingClientRect().left;
			var maxScroll = SCROLL_RANGE - viewW;

			if (maxScroll > 0)
			{
				viewX = SCROLL_MIN + (clickX / trackW) * maxScroll;
				updateViewBox();
			}

			evt.preventDefault();
			evt.stopPropagation();
		}
	});

	mxEvent.addListener(vScrollbar, 'mousedown', function(evt)
	{
		if (evt.target === vScrollbar)
		{
			var trackH = vScrollbar.getBoundingClientRect().height;
			var clickY = evt.clientY - vScrollbar.getBoundingClientRect().top;
			var maxScroll = SCROLL_RANGE - viewH;

			if (maxScroll > 0)
			{
				viewY = SCROLL_MIN + (clickY / trackH) * maxScroll;
				updateViewBox();
			}

			evt.preventDefault();
			evt.stopPropagation();
		}
	});

	var scrollMoveHandler = function(evt)
	{
		if (hScrollDragging)
		{
			var trackW = svgContainer.getBoundingClientRect().width - SCROLLBAR_SIZE;
			var thumbW = parseFloat(hThumb.style.width) || 20;
			var maxScroll = SCROLL_RANGE - viewW;

			if (trackW > thumbW && maxScroll > 0)
			{
				var dx = evt.clientX - scrollDragStart;
				viewX = scrollDragStartView + (dx / (trackW - thumbW)) * maxScroll;
				updateViewBox();
			}

			evt.preventDefault();
		}

		if (vScrollDragging)
		{
			var trackH = svgContainer.getBoundingClientRect().height - SCROLLBAR_SIZE;
			var thumbH = parseFloat(vThumb.style.height) || 20;
			var maxScroll = SCROLL_RANGE - viewH;

			if (trackH > thumbH && maxScroll > 0)
			{
				var dy = evt.clientY - scrollDragStart;
				viewY = scrollDragStartView + (dy / (trackH - thumbH)) * maxScroll;
				updateViewBox();
			}

			evt.preventDefault();
		}
	};

	var scrollUpHandler = function(evt)
	{
		hScrollDragging = false;
		vScrollDragging = false;
	};

	mxEvent.addListener(document, 'mousemove', scrollMoveHandler);
	mxEvent.addListener(document, 'mouseup', scrollUpHandler);

	function updateViewBox()
	{
		svg.setAttribute('viewBox', viewX + ' ' + viewY + ' ' + viewW + ' ' + viewH);
		updateScrollbars();
	};

	function syncViewBoxToContainer()
	{
		var rect = svg.getBoundingClientRect();

		if (rect.width > 0 && rect.height > 0)
		{
			var cx = viewX + viewW / 2;
			var cy = viewY + viewH / 2;
			viewW = rect.width * zoom;
			viewH = rect.height * zoom;
			viewX = cx - viewW / 2;
			viewY = cy - viewH / 2;
			updateViewBox();
		}
	};

	if (typeof ResizeObserver !== 'undefined')
	{
		new ResizeObserver(function() { syncViewBoxToContainer(); }).observe(svgContainer);
	}

	// Right: Point list panel
	var listPanel = document.createElement('div');
	listPanel.style.width = '200px';
	listPanel.style.flexShrink = '0';
	listPanel.style.overflowY = 'auto';
	listPanel.style.border = '1px solid';
	listPanel.style.borderColor = 'inherit';
	listPanel.style.borderRadius = '4px';
	listPanel.style.padding = '4px';
	contentDiv.appendChild(listPanel);

	function snapValue(val)
	{
		if (snapToGrid)
		{
			return Math.round(val / SNAP_SIZE) * SNAP_SIZE;
		}

		return Math.round(val);
	};

	function getSvgPoint(evt)
	{
		var rect = svg.getBoundingClientRect();
		var clientX = evt.touches != null ? evt.touches[0].clientX : evt.clientX;
		var clientY = evt.touches != null ? evt.touches[0].clientY : evt.clientY;
		var x = viewX + ((clientX - rect.left) / rect.width) * viewW;
		var y = viewY + ((clientY - rect.top) / rect.height) * viewH;

		return {x: snapValue(x), y: snapValue(y)};
	};

	function getDefaultControlPoint(prevPt, pt)
	{
		var midX = (prevPt.x + pt.x) / 2;
		var midY = (prevPt.y + pt.y) / 2;
		var dx = pt.x - prevPt.x;
		var dy = pt.y - prevPt.y;
		var len = Math.sqrt(dx * dx + dy * dy);

		if (len === 0)
		{
			return {x: midX, y: midY};
		}

		var px = -dy / len;
		var py = dx / len;
		var offset = len * 0.25;

		return {x: Math.round(midX + px * offset), y: Math.round(midY + py * offset)};
	};

	function hitTestPoint(evt)
	{
		var rect = svg.getBoundingClientRect();
		var clientX = evt.touches != null ? evt.touches[0].clientX : evt.clientX;
		var clientY = evt.touches != null ? evt.touches[0].clientY : evt.clientY;
		var rawX = viewX + ((clientX - rect.left) / rect.width) * viewW;
		var rawY = viewY + ((clientY - rect.top) / rect.height) * viewH;
		var hitRadius = (VERTEX_RADIUS + 4) * (viewW / CANVAS_SIZE);

		// Check closing segment control point
		if (closePath && points.length > 2 && points[0].type === 'Q')
		{
			var dx = points[0].cx - rawX;
			var dy = points[0].cy - rawY;

			if (Math.sqrt(dx * dx + dy * dy) <= hitRadius)
			{
				return {type: 'control', index: 0};
			}
		}

		// Check control points first (they render on top)
		for (var i = 1; i < points.length; i++)
		{
			if (points[i].type === 'Q')
			{
				var dx = points[i].cx - rawX;
				var dy = points[i].cy - rawY;

				if (Math.sqrt(dx * dx + dy * dy) <= hitRadius)
				{
					return {type: 'control', index: i};
				}
			}
		}

		for (var i = 0; i < points.length; i++)
		{
			var dx = points[i].x - rawX;
			var dy = points[i].y - rawY;

			if (Math.sqrt(dx * dx + dy * dy) <= hitRadius)
			{
				return {type: 'vertex', index: i};
			}
		}

		return null;
	};

	function pushUndo()
	{
		undoStack.push(JSON.stringify(points));
		redoStack = [];
		updateUndoButtons();
	};

	function undo()
	{
		if (undoStack.length > 0)
		{
			redoStack.push(JSON.stringify(points));
			points = JSON.parse(undoStack.pop());
			selectedIndex = Math.min(selectedIndex, points.length - 1);
			renderPolygon();
			updateUndoButtons();
		}
	};

	function redo()
	{
		if (redoStack.length > 0)
		{
			undoStack.push(JSON.stringify(points));
			points = JSON.parse(redoStack.pop());
			selectedIndex = Math.min(selectedIndex, points.length - 1);
			renderPolygon();
			updateUndoButtons();
		}
	};

	function updateUndoButtons()
	{
		if (undoBtn != null)
		{
			undoBtn.style.opacity = undoStack.length > 0 ? '1' : '0.3';
			undoBtn.style.pointerEvents = undoStack.length > 0 ? '' : 'none';
		}

		if (redoBtn != null)
		{
			redoBtn.style.opacity = redoStack.length > 0 ? '1' : '0.3';
			redoBtn.style.pointerEvents = redoStack.length > 0 ? '' : 'none';
		}
	};

	function renderPolygon()
	{
		// Update SVG path
		var d = '';

		for (var i = 0; i < points.length; i++)
		{
			if (i === 0)
			{
				d += 'M' + points[i].x + ' ' + points[i].y;
			}
			else if (points[i].type === 'Q')
			{
				d += 'Q' + points[i].cx + ' ' + points[i].cy +
					' ' + points[i].x + ' ' + points[i].y;
			}
			else
			{
				d += 'L' + points[i].x + ' ' + points[i].y;
			}
		}

		if (closePath && points.length > 2)
		{
			if (points[0].type === 'Q')
			{
				d += 'Q' + points[0].cx + ' ' + points[0].cy +
					' ' + points[0].x + ' ' + points[0].y;
			}

			d += 'Z';
		}

		polyPath.setAttribute('d', d);
		polyPath.setAttribute('fill', closePath && points.length > 2 ?
			'rgba(66, 133, 244, 0.1)' : 'none');

		// Update control point guide lines
		while (controlGuideGroup.firstChild)
		{
			controlGuideGroup.removeChild(controlGuideGroup.firstChild);
		}

		for (var i = 1; i < points.length; i++)
		{
			if (points[i].type === 'Q')
			{
				var guideLine = document.createElementNS(svgNS, 'polyline');
				guideLine.setAttribute('points',
					points[i - 1].x + ',' + points[i - 1].y + ' ' +
					points[i].cx + ',' + points[i].cy + ' ' +
					points[i].x + ',' + points[i].y);
				guideLine.setAttribute('fill', 'none');
				guideLine.setAttribute('stroke', '#f0a030');
				guideLine.setAttribute('stroke-width', '1');
				guideLine.setAttribute('stroke-dasharray', '4,3');
				controlGuideGroup.appendChild(guideLine);
			}
		}

		if (closePath && points.length > 2 && points[0].type === 'Q')
		{
			var closingGuide = document.createElementNS(svgNS, 'polyline');
			closingGuide.setAttribute('points',
				points[points.length - 1].x + ',' + points[points.length - 1].y + ' ' +
				points[0].cx + ',' + points[0].cy + ' ' +
				points[0].x + ',' + points[0].y);
			closingGuide.setAttribute('fill', 'none');
			closingGuide.setAttribute('stroke', '#f0a030');
			closingGuide.setAttribute('stroke-width', '1');
			closingGuide.setAttribute('stroke-dasharray', '4,3');
			controlGuideGroup.appendChild(closingGuide);
		}

		// Update vertex circles
		while (vertexGroup.firstChild)
		{
			vertexGroup.removeChild(vertexGroup.firstChild);
		}

		for (var i = 0; i < points.length; i++)
		{
			var circle = document.createElementNS(svgNS, 'circle');
			circle.setAttribute('cx', points[i].x);
			circle.setAttribute('cy', points[i].y);
			circle.setAttribute('r', VERTEX_RADIUS);
			circle.setAttribute('fill', i === selectedIndex ? '#ff4444' : '#4285f4');
			circle.setAttribute('stroke', '#fff');
			circle.setAttribute('stroke-width', '1.5');
			circle.style.cursor = 'move';
			vertexGroup.appendChild(circle);
		}

		// Update control point handles
		while (controlPointGroup.firstChild)
		{
			controlPointGroup.removeChild(controlPointGroup.firstChild);
		}

		for (var i = 1; i < points.length; i++)
		{
			if (points[i].type === 'Q')
			{
				var handle = document.createElementNS(svgNS, 'rect');
				handle.setAttribute('x', points[i].cx - 5);
				handle.setAttribute('y', points[i].cy - 5);
				handle.setAttribute('width', 10);
				handle.setAttribute('height', 10);
				handle.setAttribute('fill', i === selectedIndex ? '#ff8800' : '#f0a030');
				handle.setAttribute('stroke', '#fff');
				handle.setAttribute('stroke-width', '1.5');
				handle.style.cursor = 'move';
				controlPointGroup.appendChild(handle);
			}
		}

		if (closePath && points.length > 2 && points[0].type === 'Q')
		{
			var closingHandle = document.createElementNS(svgNS, 'rect');
			closingHandle.setAttribute('x', points[0].cx - 5);
			closingHandle.setAttribute('y', points[0].cy - 5);
			closingHandle.setAttribute('width', 10);
			closingHandle.setAttribute('height', 10);
			closingHandle.setAttribute('fill', 0 === selectedIndex ? '#ff8800' : '#f0a030');
			closingHandle.setAttribute('stroke', '#fff');
			closingHandle.setAttribute('stroke-width', '1.5');
			closingHandle.style.cursor = 'move';
			controlPointGroup.appendChild(closingHandle);
		}

		updatePointList();
	};

	function updatePointList()
	{
		listPanel.innerHTML = '';

		if (points.length === 0)
		{
			var hint = document.createElement('div');
			hint.style.padding = '20px 10px';
			hint.style.textAlign = 'center';
			hint.style.opacity = '0.5';
			hint.style.fontSize = '12px';
			mxUtils.write(hint, mxResources.get('clickToAdd'));
			listPanel.appendChild(hint);
			return;
		}

		for (var i = 0; i < points.length; i++)
		{
			(function(idx)
			{
				var row = document.createElement('div');
				row.style.display = 'flex';
				row.style.alignItems = 'center';
				row.style.padding = '2px 4px';
				row.style.gap = '4px';
				row.style.borderRadius = '3px';
				row.style.marginBottom = '2px';
				row.style.fontSize = '12px';
				row.setAttribute('draggable', 'true');
				row.setAttribute('data-idx', idx);

				if (idx === selectedIndex)
				{
					row.style.backgroundColor = 'rgba(66, 133, 244, 0.15)';
				}

				row.style.cursor = 'pointer';

				mxEvent.addListener(row, 'click', function()
				{
					selectedIndex = idx;
					renderPolygon();
				});

				// Drag handle
				var dragHandle = document.createElement('span');
				dragHandle.innerHTML = '&#9776;';
				dragHandle.style.cursor = 'grab';
				dragHandle.style.fontSize = '11px';
				dragHandle.style.opacity = '0.4';
				dragHandle.style.flexShrink = '0';
				row.appendChild(dragHandle);

				var label = document.createElement('span');
				label.style.minWidth = '18px';
				label.style.fontWeight = 'bold';
				mxUtils.write(label, (idx + 1) + '');
				row.appendChild(label);

				var xInput = document.createElement('input');
				xInput.type = 'number';
				xInput.value = Math.round(points[idx].x);
				xInput.style.width = '48px';
				xInput.style.fontSize = '11px';
				xInput.style.padding = '1px 3px';
				xInput.title = 'X';

				mxEvent.addListener(xInput, 'change', function()
				{
					pushUndo();
					points[idx].x = parseInt(this.value) || 0;
					renderPolygon();
				});

				mxEvent.addListener(xInput, 'click', function(e)
				{
					e.stopPropagation();
				});

				row.appendChild(xInput);

				var yInput = document.createElement('input');
				yInput.type = 'number';
				yInput.value = Math.round(points[idx].y);
				yInput.style.width = '48px';
				yInput.style.fontSize = '11px';
				yInput.style.padding = '1px 3px';
				yInput.title = 'Y';

				mxEvent.addListener(yInput, 'change', function()
				{
					pushUndo();
					points[idx].y = parseInt(this.value) || 0;
					renderPolygon();
				});

				mxEvent.addListener(yInput, 'click', function(e)
				{
					e.stopPropagation();
				});

				row.appendChild(yInput);

				// Curve toggle (for closing segment when idx is 0 and path is closed)
				if (idx > 0 || (idx === 0 && closePath && points.length > 2))
				{
					var curveLabel = document.createElement('label');
					curveLabel.style.display = 'flex';
					curveLabel.style.alignItems = 'center';
					curveLabel.style.cursor = 'pointer';
					curveLabel.style.flexShrink = '0';
					curveLabel.title = mxResources.get('curved');

					var curveCb = document.createElement('input');
					curveCb.type = 'checkbox';
					curveCb.checked = points[idx].type === 'Q';
					curveCb.style.margin = '0';

					mxEvent.addListener(curveCb, 'click', function(e)
					{
						e.stopPropagation();
					});

					mxEvent.addListener(curveCb, 'change', function()
					{
						pushUndo();

						if (points[idx].type === 'Q')
						{
							points[idx].type = 'L';
							delete points[idx].cx;
							delete points[idx].cy;
						}
						else
						{
							var prevPt = idx > 0 ? points[idx - 1] : points[points.length - 1];
							var defCp = getDefaultControlPoint(prevPt, points[idx]);
							points[idx].type = 'Q';
							points[idx].cx = defCp.x;
							points[idx].cy = defCp.y;
						}

						renderPolygon();
					});

					curveLabel.appendChild(curveCb);
					row.appendChild(curveLabel);
				}

				// Delete button
				var delBtn = document.createElement('img');
				delBtn.setAttribute('src', Editor.trashImage);
				delBtn.style.cursor = 'pointer';
				delBtn.style.marginLeft = 'auto';
				delBtn.style.width = '14px';
				delBtn.style.height = '14px';
				delBtn.style.opacity = '0.5';
				delBtn.style.flexShrink = '0';
				delBtn.title = mxResources.get('delete');

				if (Editor.isDarkMode())
				{
					delBtn.style.filter = 'invert(1)';
				}

				mxEvent.addListener(delBtn, 'click', function(e)
				{
					e.stopPropagation();
					pushUndo();
					points.splice(idx, 1);

					if (selectedIndex >= points.length)
					{
						selectedIndex = points.length - 1;
					}

					renderPolygon();
				});

				row.appendChild(delBtn);

				// Drag-and-drop handlers
				mxEvent.addListener(row, 'dragstart', function(e)
				{
					listDragSource = idx;
					row.style.opacity = '0.4';
					e.dataTransfer.effectAllowed = 'move';
					e.dataTransfer.setData('text/plain', '' + idx);
					e.stopPropagation();
				});

				mxEvent.addListener(row, 'dragover', function(e)
				{
					e.preventDefault();
					e.stopPropagation();
					e.dataTransfer.dropEffect = 'move';
					var targetIdx = parseInt(row.getAttribute('data-idx'));

					if (listDragSource !== null && targetIdx !== listDragSource)
					{
						row.style.borderTop = targetIdx < listDragSource ?
							'2px solid #4285f4' : '';
						row.style.borderBottom = targetIdx > listDragSource ?
							'2px solid #4285f4' : '';
					}
				});

				mxEvent.addListener(row, 'dragleave', function(e)
				{
					row.style.borderTop = '';
					row.style.borderBottom = '';
				});

				mxEvent.addListener(row, 'drop', function(e)
				{
					e.preventDefault();
					e.stopPropagation();
					row.style.borderTop = '';
					row.style.borderBottom = '';
					var targetIdx = parseInt(row.getAttribute('data-idx'));

					if (listDragSource !== null && targetIdx !== listDragSource)
					{
						pushUndo();
						var movedPoint = points.splice(listDragSource, 1)[0];
						var insertIdx = targetIdx > listDragSource ? targetIdx - 1 : targetIdx;
						points.splice(insertIdx, 0, movedPoint);

						if (selectedIndex === listDragSource)
						{
							selectedIndex = insertIdx;
						}
						else if (listDragSource < selectedIndex && insertIdx >= selectedIndex)
						{
							selectedIndex--;
						}
						else if (listDragSource > selectedIndex && insertIdx <= selectedIndex)
						{
							selectedIndex++;
						}

						renderPolygon();
					}

					listDragSource = null;
				});

				mxEvent.addListener(row, 'dragend', function(e)
				{
					row.style.opacity = '';
					row.style.borderTop = '';
					row.style.borderBottom = '';
					listDragSource = null;
				});

				listPanel.appendChild(row);
			})(i);
		}
	};

	// Mouse handling on SVG canvas
	mxEvent.addGestureListeners(svg, function(evt)
	{
		if (spacePressed || evt.button === 1 || evt.button === 2) return;

		var hit = hitTestPoint(evt);

		if (hit != null)
		{
			// Start dragging an existing point
			dragIndex = hit.index;
			dragType = hit.type;
			isDragging = true;
			dragStarted = false;
			selectedIndex = hit.index;
			renderPolygon();
		}
		else
		{
			var pt = getSvgPoint(evt);
			pushUndo();

			if (points.length < 2)
			{
				// Not enough points for segments, just append
				points.push({x: pt.x, y: pt.y, type: 'L'});
				selectedIndex = points.length - 1;
			}
			else
			{
				// Find nearest segment and insert there
				var bestDist = Infinity;
				var bestSeg = points.length - 1;
				var segCount = closePath ? points.length : points.length - 1;

				for (var si = 0; si < segCount; si++)
				{
					var ni = (si + 1) % points.length;
					var d = mxUtils.ptSegDistSq(points[si].x, points[si].y,
						points[ni].x, points[ni].y, pt.x, pt.y);

					if (d < bestDist)
					{
						bestDist = d;
						bestSeg = si;
					}
				}

				points.splice(bestSeg + 1, 0, {x: pt.x, y: pt.y, type: 'L'});
				selectedIndex = bestSeg + 1;
			}

			dragIndex = selectedIndex;
			dragType = 'vertex';
			isDragging = true;
			dragStarted = true;
			renderPolygon();
		}

		mxEvent.consume(evt);
	},
	function(evt)
	{
		if (isDragging && dragIndex >= 0)
		{
			if (!dragStarted)
			{
				pushUndo();
				dragStarted = true;
			}

			var pt = getSvgPoint(evt);

			if (dragType === 'control')
			{
				points[dragIndex].cx = pt.x;
				points[dragIndex].cy = pt.y;
			}
			else
			{
				points[dragIndex].x = pt.x;
				points[dragIndex].y = pt.y;
			}

			renderPolygon();
			mxEvent.consume(evt);
		}
	},
	function(evt)
	{
		if (spacePressed || evt.button === 1 || evt.button === 2) return;

		if (isDragging && dragIndex >= 0)
		{
			isDragging = false;
			dragIndex = -1;
			dragType = null;
			mxEvent.consume(evt);
		}
	});

	// Mouse wheel zoom on SVG
	mxEvent.addListener(svg, 'wheel', function(evt)
	{
		evt.preventDefault();
		var rect = svg.getBoundingClientRect();
		var px = (evt.clientX - rect.left) / rect.width;
		var py = (evt.clientY - rect.top) / rect.height;
		var cursorX = viewX + px * viewW;
		var cursorY = viewY + py * viewH;

		var factor = evt.deltaY > 0 ? 1.15 : 1 / 1.15;
		zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * factor));

		viewW = rect.width * zoom;
		viewH = rect.height * zoom;
		viewX = cursorX - px * viewW;
		viewY = cursorY - py * viewH;
		updateViewBox();
	});

	// Suppress context menu on SVG canvas
	mxEvent.addListener(svg, 'contextmenu', function(evt)
	{
		evt.preventDefault();
	});

	// Pan (middle-click, right-click or Space+left-click)
	mxEvent.addListener(svg, 'mousedown', function(evt)
	{
		if (evt.button === 1 || evt.button === 2 || (evt.button === 0 && spacePressed))
		{
			isPanning = true;
			panStartX = evt.clientX;
			panStartY = evt.clientY;
			panStartViewX = viewX;
			panStartViewY = viewY;
			svg.style.cursor = 'grabbing';
			evt.preventDefault();
		}
	});

	var panMoveHandler = function(evt)
	{
		if (isPanning)
		{
			var rect = svg.getBoundingClientRect();
			viewX = panStartViewX - (evt.clientX - panStartX) / rect.width * viewW;
			viewY = panStartViewY - (evt.clientY - panStartY) / rect.height * viewH;
			updateViewBox();
			evt.preventDefault();
		}
	};

	var panUpHandler = function(evt)
	{
		if (isPanning)
		{
			isPanning = false;
			svg.style.cursor = spacePressed ? 'grab' : 'crosshair';
		}
	};

	mxEvent.addListener(document, 'mousemove', panMoveHandler);
	mxEvent.addListener(document, 'mouseup', panUpHandler);

	// Bottom bar with all controls
	var bottomBar = document.createElement('div');
	bottomBar.style.display = 'flex';
	bottomBar.style.alignItems = 'center';
	bottomBar.style.gap = '6px';
	bottomBar.style.position = 'absolute';
	bottomBar.style.left = '0px';
	bottomBar.style.right = '0px';
	bottomBar.style.bottom = '0px';
	bottomBar.style.height = '34px';

	var undoBtn = editorUi.createToolbarButton(Editor.undoImage,
		mxResources.get('undo'), function() { undo(); });
	bottomBar.appendChild(undoBtn);

	var redoBtn = editorUi.createToolbarButton(Editor.redoImage,
		mxResources.get('redo'), function() { redo(); });
	bottomBar.appendChild(redoBtn);

	var zoomInBtn = editorUi.createToolbarButton(Editor.zoomInImage,
		mxResources.get('zoomIn'), function()
	{
		zoomTo(zoom / 1.15);
	});
	bottomBar.appendChild(zoomInBtn);

	var zoomOutBtn = editorUi.createToolbarButton(Editor.zoomOutImage,
		mxResources.get('zoomOut'), function()
	{
		zoomTo(zoom * 1.15);
	});
	bottomBar.appendChild(zoomOutBtn);

	var zoomFitBtn = editorUi.createToolbarButton(Editor.zoomFitImage,
		mxResources.get('fit'), function()
	{
		var rect = svg.getBoundingClientRect();

		if (rect.width > 0 && rect.height > 0)
		{
			if (zoom == 1)
			{
				// Fit: compute bounding box of points and fit to view
				if (points.length > 0)
				{
					var minX = points[0].x, minY = points[0].y;
					var maxX = points[0].x, maxY = points[0].y;

					for (var i = 1; i < points.length; i++)
					{
						minX = Math.min(minX, points[i].x);
						minY = Math.min(minY, points[i].y);
						maxX = Math.max(maxX, points[i].x);
						maxY = Math.max(maxY, points[i].y);

						if (points[i].type === 'Q')
						{
							minX = Math.min(minX, points[i].cx);
							minY = Math.min(minY, points[i].cy);
							maxX = Math.max(maxX, points[i].cx);
							maxY = Math.max(maxY, points[i].cy);
						}
					}

					var pad = 20;
					var bw = maxX - minX + pad * 2;
					var bh = maxY - minY + pad * 2;
					var sx = rect.width / bw;
					var sy = rect.height / bh;
					zoom = 1 / Math.min(sx, sy);
					viewW = rect.width * zoom;
					viewH = rect.height * zoom;
					viewX = (minX - pad) + (bw - viewW) / 2;
					viewY = (minY - pad) + (bh - viewH) / 2;
				}
				else
				{
					zoom = 1;
					viewW = rect.width;
					viewH = rect.height;
					viewX = (CANVAS_SIZE - viewW) / 2;
					viewY = (CANVAS_SIZE - viewH) / 2;
				}
			}
			else
			{
				// Reset to zoom=1
				zoom = 1;
				viewW = rect.width * zoom;
				viewH = rect.height * zoom;
				viewX = (CANVAS_SIZE - viewW) / 2;
				viewY = (CANVAS_SIZE - viewH) / 2;
			}

			updateViewBox();
		}
	});
	bottomBar.appendChild(zoomFitBtn);

	var deleteAllBtn = mxUtils.button(mxResources.get('deleteAll'), function()
	{
		if (points.length > 0)
		{
			pushUndo();
			points = [];
			selectedIndex = -1;
			renderPolygon();
		}
	});
	deleteAllBtn.className = 'geBtn';
	deleteAllBtn.style.padding = '2px 10px';
	deleteAllBtn.style.fontSize = '12px';
	bottomBar.appendChild(deleteAllBtn);

	// Separator
	var sep = document.createElement('span');
	sep.style.flexGrow = '1';
	bottomBar.appendChild(sep);

	// Snap to grid checkbox
	var snapLabel = document.createElement('label');
	snapLabel.style.display = 'flex';
	snapLabel.style.alignItems = 'center';
	snapLabel.style.gap = '4px';
	snapLabel.style.fontSize = '12px';
	snapLabel.style.cursor = 'pointer';
	snapLabel.style.whiteSpace = 'nowrap';

	var snapCb = document.createElement('input');
	snapCb.type = 'checkbox';
	snapCb.checked = snapToGrid;

	mxEvent.addListener(snapCb, 'change', function()
	{
		snapToGrid = this.checked;
		gridRect.style.display = snapToGrid ? '' : 'none';
	});

	snapLabel.appendChild(snapCb);
	mxUtils.write(snapLabel, mxResources.get('grid'));
	bottomBar.appendChild(snapLabel);

	// Close path checkbox
	var closeLabel = document.createElement('label');
	closeLabel.style.display = 'flex';
	closeLabel.style.alignItems = 'center';
	closeLabel.style.gap = '4px';
	closeLabel.style.fontSize = '12px';
	closeLabel.style.cursor = 'pointer';
	closeLabel.style.whiteSpace = 'nowrap';

	var closeCb = document.createElement('input');
	closeCb.type = 'checkbox';
	closeCb.checked = closePath;

	mxEvent.addListener(closeCb, 'change', function()
	{
		closePath = this.checked;
		renderPolygon();
	});

	closeLabel.appendChild(closeCb);
	mxUtils.write(closeLabel, mxResources.get('closePath'));
	bottomBar.appendChild(closeLabel);

	// Apply/Cancel buttons
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	cancelBtn.className = 'geBtn';

	var applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		if (points.length < 2)
		{
			editorUi.showError(mxResources.get('error'),
				mxResources.get('minTwoPoints'),
				mxResources.get('ok'));
			return;
		}

		// Compute bounding box of all points and control points
		var bminX = points[0].x, bmaxX = points[0].x;
		var bminY = points[0].y, bmaxY = points[0].y;

		for (var i = 0; i < points.length; i++)
		{
			bminX = Math.min(bminX, points[i].x);
			bmaxX = Math.max(bmaxX, points[i].x);
			bminY = Math.min(bminY, points[i].y);
			bmaxY = Math.max(bmaxY, points[i].y);

			if (points[i].type === 'Q')
			{
				bminX = Math.min(bminX, points[i].cx);
				bmaxX = Math.max(bmaxX, points[i].cx);
				bminY = Math.min(bminY, points[i].cy);
				bmaxY = Math.max(bmaxY, points[i].cy);
			}
		}

		var rangeX = bmaxX - bminX;
		var rangeY = bmaxY - bminY;

		// Normalize coordinates to bounding box (0-1 range)
		var nx = function(val)
		{
			return rangeX > 0 ? Math.round(((val - bminX) / rangeX) * 100) / 100 : 0.5;
		};

		var ny = function(val)
		{
			return rangeY > 0 ? Math.round(((val - bminY) / rangeY) * 100) / 100 : 0.5;
		};

		var newCoords = [];
		var newCurves = [];

		for (var i = 0; i < points.length; i++)
		{
			newCoords.push([nx(points[i].x), ny(points[i].y)]);

			if (i > 0)
			{
				if (points[i].type === 'Q')
				{
					newCurves.push(['Q', nx(points[i].cx), ny(points[i].cy)]);
				}
				else
				{
					newCurves.push([]);
				}
			}
		}

		// Handle closing segment curve
		if (closePath && points.length > 2 && points[0].type === 'Q')
		{
			newCurves.push(['Q', nx(points[0].cx), ny(points[0].cy)]);
		}

		// Update cell geometry to maintain visual position and size
		var geo = graph.getCellGeometry(cell);

		if (geo != null)
		{
			geo = geo.clone();

			if (rangeX > 0)
			{
				geo.x = geo.x + (bminX / CANVAS_SIZE) * geo.width;
				geo.width = (rangeX / CANVAS_SIZE) * geo.width;
			}

			if (rangeY > 0)
			{
				geo.y = geo.y + (bminY / CANVAS_SIZE) * geo.height;
				geo.height = (rangeY / CANVAS_SIZE) * geo.height;
			}
		}

		graph.getModel().beginUpdate();

		try
		{
			if (geo != null)
			{
				graph.getModel().setGeometry(cell, geo);
			}

			graph.setCellStyles('polyCoords', JSON.stringify(newCoords), [cell]);
			graph.setCellStyles('polyCurves', JSON.stringify(newCurves), [cell]);
			graph.setCellStyles('polyline', closePath ? '0' : '1', [cell]);
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		editorUi.hideDialog();
	});
	applyBtn.className = 'geBtn gePrimaryBtn';

	if (editorUi.editor.cancelFirst)
	{
		bottomBar.appendChild(cancelBtn);
		bottomBar.appendChild(applyBtn);
	}
	else
	{
		bottomBar.appendChild(applyBtn);
		bottomBar.appendChild(cancelBtn);
	}

	div.appendChild(bottomBar);

	// Keyboard handling
	mxEvent.addListener(div, 'keydown', function(evt)
	{
		if (evt.keyCode === 32) // Space
		{
			if (!spacePressed)
			{
				spacePressed = true;
				svg.style.cursor = 'grab';
			}

			mxEvent.consume(evt);
			return;
		}

		if (evt.keyCode === 46 || evt.keyCode === 8) // Delete/Backspace
		{
			if (selectedIndex >= 0 && selectedIndex < points.length)
			{
				if (evt.target.tagName !== 'INPUT')
				{
					pushUndo();
					points.splice(selectedIndex, 1);

					if (selectedIndex >= points.length)
					{
						selectedIndex = points.length - 1;
					}

					renderPolygon();
					mxEvent.consume(evt);
				}
			}
		}
		else if (evt.keyCode === 90 && (evt.ctrlKey || evt.metaKey)) // Ctrl+Z
		{
			if (evt.shiftKey)
			{
				redo();
			}
			else
			{
				undo();
			}

			mxEvent.consume(evt);
		}
		else if (evt.keyCode === 89 && (evt.ctrlKey || evt.metaKey)) // Ctrl+Y
		{
			redo();
			mxEvent.consume(evt);
		}
	});

	mxEvent.addListener(div, 'keyup', function(evt)
	{
		if (evt.keyCode === 32)
		{
			spacePressed = false;

			if (!isPanning)
			{
				svg.style.cursor = 'crosshair';
			}
		}
	});

	// Initial render
	renderPolygon();
	updateUndoButtons();

	this.init = function()
	{
		div.focus();
		var rect = svg.getBoundingClientRect();

		if (rect.width > 0 && rect.height > 0)
		{
			zoom = 1;
			viewW = rect.width * zoom;
			viewH = rect.height * zoom;
			viewX = (CANVAS_SIZE - viewW) / 2;
			viewY = (CANVAS_SIZE - viewH) / 2;
			updateViewBox();
		}
	};

	this.destroy = function()
	{
		mxEvent.removeListener(document, 'mousemove', scrollMoveHandler);
		mxEvent.removeListener(document, 'mouseup', scrollUpHandler);
		mxEvent.removeListener(document, 'mousemove', panMoveHandler);
		mxEvent.removeListener(document, 'mouseup', panUpHandler);
	};

	this.container = div;
};
