/**
 * Copyright (c) 2006-2012, JGraph Holdings Ltd
 */
/**
 * Constructs a new open dialog.
 */
var OpenDialog = function()
{
	var iframe = document.createElement('iframe');
	iframe.style.backgroundColor = 'transparent';
	iframe.allowTransparency = 'true';
	iframe.style.borderStyle = 'none';
	iframe.style.borderWidth = '0px';
	iframe.style.overflow = 'hidden';
	iframe.style.maxWidth = '100%';
	iframe.frameBorder = '0';
	
	var dx = 0;
	iframe.setAttribute('width', (((Editor.useLocalStorage) ? 640 : 320) + dx) + 'px');
	iframe.setAttribute('height', (((Editor.useLocalStorage) ? 480 : 220) + dx) + 'px');
	iframe.setAttribute('src', OPEN_FORM);
	
	this.container = iframe;
};

/**
 * 
 */
var ColorPicker = function()
{
	mxEventSource.call(this);

	var div = document.createElement('div');

	var x0 = 6;
	var y0 = 4;

	div.style.overflow = 'visible';
	div.style.position = 'relative';
	div.style.top = '0px';
	div.style.left = '0px';
	div.style.width = '230px';
	div.style.height = '114px';

	var colorPanel = document.createElement('div');

	colorPanel.style.border = '1px solid light-dark(black, white)';
	colorPanel.style.backgroundImage = 'url(' + this.hsImage + ')';
	colorPanel.style.position = 'absolute';
	colorPanel.style.overflow = 'hidden';
	colorPanel.style.cursor = 'crosshair';
	colorPanel.style.width = '181px';
	colorPanel.style.height = '101px';
	colorPanel.style.left = x0 + 'px';
	colorPanel.style.top = y0 + 'px';

	var cross = document.createElement('div');
	cross.style.background = 'url(' + this.crossImage + ')';
	cross.style.pointerEvents = 'none';
	cross.style.position = 'absolute';
	cross.style.width = '15px';
	cross.style.height = '15px';
	cross.style.left = '-8px';
	cross.style.top = '-8px';

	var currentHsv = [0, 0, 1];
	var currentAlpha = 1;
	
	function hsv2rgb(h, s, v)
	{
		if (h == null)
		{
			return [v, v, v];
		}

		var i = Math.floor(h);
		var f = i % 2 ? h - i : 1 - (h - i);
		var m = v * (1 - s);
		var n = v * (1 - s * f);

		switch(i)
		{
			case 6:
			case 0: return [v, n, m];
			case 1: return [n, v, m];
			case 2: return [m, v, n];
			case 3: return [m, n, v];
			case 4: return [n, m, v];
			case 5: return [v, m, n];
		}
	};

	function rgb2hsv(r, g, b)
	{
		var n = Math.min(Math.min(r, g), b);
		var v = Math.max(Math.max(r, g), b);
		var m = v - n;

		if (m === 0)
		{
			return [null, 0, v];
		}

		var h = r === n ? 3 + (b - g) / m :
			(g === n ? 5 + (r - b) / m :
				1 + (g - r) / m);
		
		return [h === 6 ? 0 : h, m / v, v];
	};

	function fromHSV(h, s, v)
	{
		h < 0 && (h = 0) || h > 6 && (h = 6);
		s < 0 && (s = 0) || s > 1 && (s = 1);
		v < 0 && (v = 0) || v > 1 && (v = 1);

		return hsv2rgb(
			h == null ? currentHsv[0] : (currentHsv[0] = h),
			s == null ? currentHsv[1] : (currentHsv[1] = s),
			v == null ? currentHsv[2] : (currentHsv[2] = v)
		);
	};

	colorPanel.appendChild(cross);
	div.appendChild(colorPanel);

	sliderBox = document.createElement('div');
	sliderBox.style.position = 'absolute';
	sliderBox.style.cursor = 'pointer';
	sliderBox.style.width = '38px';
	sliderBox.style.height = '110px';
	sliderBox.style.left = (193 + x0) + 'px';
	sliderBox.style.top = y0 + 'px';

	var sliderPanel = document.createElement('div');
	sliderPanel.style.border = '1px solid light-dark(black, white)';
	sliderPanel.style.pointerEvents = 'none';
	sliderPanel.style.overflow = 'hidden';
	sliderPanel.style.position = 'absolute';
	sliderPanel.style.width = '16px';
	sliderPanel.style.height = '101px';
	sliderPanel.style.left = '9px';
	sliderPanel.style.top = '0px';

	var panelActive = false;
	var sliderActive = false;

	var update = mxUtils.bind(this, function(quiet)
	{
		var x = Math.round((currentHsv[0] / 6) * 180);
		var y = Math.round((1 - currentHsv[1]) * 100);

		cross.style.left = (x - 8) + 'px';
		cross.style.top = (y - 8) + 'px';

		var seg = sliderPanel.childNodes;
		var arr = hsv2rgb(currentHsv[0], currentHsv[1], 1);
		
		if (arr != null)
		{
			for (var i = 0; i < seg.length; i++)
			{
				seg[i].style.backgroundColor = 'rgb('+
					(arr[0] * (1 - i / seg.length)) * 255 + ', ' +
					(arr[1] * (1 - i / seg.length)) * 255 + ', ' +
					(arr[2] * (1 - i / seg.length)) * 255 + ')';
			}

			var arrowY = Math.round((1 - currentHsv[2]) * 100);
			arrow.style.top = (arrowY - 4) + 'px';

			var rgb = hsv2rgb(currentHsv[0], currentHsv[1], currentHsv[2]);
			var color = '#' +
				('0' + Math.round(rgb[0] * 255).toString(16)).slice(-2) +
				('0' + Math.round(rgb[1] * 255).toString(16)).slice(-2) +
				('0' + Math.round(rgb[2] * 255).toString(16)).slice(-2) +
				(currentAlpha < 1 ?
					('0' + Math.round(currentAlpha * 255).toString(16)).slice(-2) : '');

			if (quiet != true)
			{
				this.fireEvent(new mxEventObject('change',
					'color', mxUtils.rgba2hex(color)));
			}
		}
	});

	var panelMouseDown = mxUtils.bind(this, function(e)
	{
		if (currentHsv[2] == 0)
		{
			currentHsv[2] = 1;
		}
		
		panelActive = true;
		mouseMove(e);
	});

	var sliderMouseDown = mxUtils.bind(this, function(e)
	{
		sliderActive = true;
		mouseMove(e);
	});

	var mouseMove = mxUtils.bind(this, function(e)
	{
		if (panelActive)
		{
			var bounds = colorPanel.getBoundingClientRect();
			var x = mxEvent.getClientX(e) - bounds.left;
			var y = mxEvent.getClientY(e) - bounds.top;

			cross.style.left = (x - 8) + 'px';
			cross.style.top = (y - 8) + 'px';

			var temp = fromHSV(x * (6 / 180), 1 - y / 100);

			this.fromString('#' +
				('0' + Math.floor(temp[0] * 255).toString(16)).slice(-2) +
				('0' + Math.floor(temp[1] * 255).toString(16)).slice(-2) +
				('0' + Math.floor(temp[2] * 255).toString(16)).slice(-2) +
				(currentAlpha < 1 ?
					('0' + Math.round(currentAlpha * 255).toString(16)).slice(-2) : ''));
		}
		else if (sliderActive)
		{
			var bounds = sliderBox.getBoundingClientRect();
			var y = mxEvent.getClientY(e) - bounds.top;
			currentHsv[2] = Math.max(Math.min(1, 1 - y / 100), 0);
			update();
		}
	});

	var mouseUp = mxUtils.bind(this, function()
	{
		sliderActive = false;
		panelActive = false;
	});

	mxEvent.addGestureListeners(colorPanel, panelMouseDown);
	mxEvent.addGestureListeners(sliderBox, sliderMouseDown);
	mxEvent.addGestureListeners(div, null, mouseMove, mouseUp);
	mxEvent.addListener(div, 'mouseleave', mouseUp);

	var tileHeight = 4;

	for(var i = 0; i < 101; i += tileHeight)
	{
		var seg = document.createElement('div');
		seg.style.height = tileHeight + 'px';
		seg.style.pointerEvents = 'none';
		sliderPanel.appendChild(seg);
	}

	div.appendChild(sliderPanel);

	var arrow = document.createElement('div');
	arrow.style.background = 'url(' + this.arrowImage + ')';
	arrow.style.position = 'absolute';
	arrow.style.pointerEvents = 'none';
	arrow.style.width = '7px';
	arrow.style.height = '11px';
	arrow.style.left = '2px';
	arrow.style.top = '-3px';

	sliderBox.appendChild(arrow);
	sliderBox.appendChild(sliderPanel);
	div.appendChild(sliderBox);

	this.fromString = function(color, quiet)
	{
		quiet = (quiet != null) ? quiet : false;

		if (color == null || color == '' ||
			mxUtils.isVarColor(color) ||
			color == mxConstants.NONE)
		{
			color = '#ffffff';
			quiet = true;
		}
		
		var rgb = mxUtils.parseColor(color);
		var hsv = rgb2hsv(rgb.r / 255, rgb.g / 255, rgb.b / 255);

		if (hsv[0] != null)
		{
			currentHsv[0] = hsv[0];
		}
		
		if (hsv[2] != 0)
		{
			currentHsv[1] = hsv[1];
		}

		currentHsv[2] = hsv[2];
		currentAlpha = rgb.a;
		update(quiet);
	};

	this.fromString('#ffffff');
	this.div = div;
};

// Extends mxEventSource
mxUtils.extend(ColorPicker, mxEventSource);

/**
 * Contains the HS image.
 */
ColorPicker.prototype.hsImage = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAALUAAABlCAIAAACEDzXRAAAKQ0lEQVR42u2d23IjKwxFBeRh5v8/9uQlzXlI2gGELoCEm6pxubp6PI69WoV3C20uIQPAH4A/AH/p41/pDcTxP4BPGDtq3vYJGPpv8craeRdo/fyTI8ZhHnodOGgcV/3rnx8QI0SABO2xfAbiCfVR8Wj+CD8jenbRJNyGOxLoAaH1iLu4EUEHAv1+MsQ4zLE4AiIOcpibv4u9OET6+cP4ASnJ1JH4/G60A9km+GbBxLlpKAkADoR2JnaBrvWD/x1GCZn9EYLEHqVfI6Efx0D7E9tDF/rBSJ54l8FqTUse0J+EvxPrdK0fD4aOLXQi7zyGxMaRvvWjjH2U8o9I44OKnf+wyCpHoR/Phk6d/COq8481YrNI3/qR2B+kMl1Fl6J8o/JrU/GEA6HdiB2ha/0YBVx7rFwNHAj9JuIl6EI/qKyJug4Yvg7xA0QEpB8nQe8itoQu9EOT8M3mTsqUSUzykH6cBO1PbA9d91+ShDzV9xrtcgW21IT6L0yfcS90FKCdw+wS6Q9ISWjVfLTVVT2mdiPGGRduFKWm7dBStWlLmI0j/QExdgp7gT5qCu2BLKiLqtc94hI7PB8auQP4KuyIvSJ960fUqXWcd2F48Khr1bHSj2dDp/afmg7tMrFxpG/9mHMFuqrH3spFw0s0Bgr9OAzamdgFutaPpMuqo6WVEXWJdSL14xhof2J76MKfS2yXWGOVB1WXXGOVdxFSx587CXoLsTE0689RH7VW4RMLeKI9wPpzz4XeS2wDzdbXR52AWStjwiSAA6HdiB2h1f6cqT0whzzuzz0LejuxATTy90Xt40v7CzdGRvKS4O/zQ0AiXaRW5x/dv4vS4A/a308LYR5pFquRRvlHIrKWoCj5woCVId4eR/KPNDjURg2tdEC7rSQN5B9DYR70X5YivVxft7AyrOvrD4V2JnaBJurr+qpemLEywlRhT6qvPx16C7ExdK0fSjcj0maAzsqgBt7rfIxGP46B9iT2gi70I842bNj0U4wd/WAG/L5VP3p0W4iNoYv6Oh6eoJmSMW5laIzF8iT1TtgeV9RZoZ7QvdEUzsQu0CP+HOMKDFoZvDFg6s89CNqf2B5a4e8Hts84W0pgeohBtspBXYp8H3Tk/H23MBtHuvbn5mp7dlU9TUkP+XNJMabzHdCIbnuYDSItzZ+bdgUWrAwNPhwInRxw1/wXuYGj+4vo7flboRpjEQ6EjgojYK9/K04NFfNTauEBypXQWQLAfqQoHl9CfvpQ6C3ExtC9/q2YW2u6X1JWzfsYFPKX3L99LrQ/sT10XR+jKh+UN2AxlDOyXfLQaxxf/fkvT4d2JnaBZv258M6h4IFu2Em7/sezoLcQG0Mjfy6xrqLnVBLGWHw1i69WP5LCCn0TNEL3DLNXpCV/n5oo5jAVLY5Em6i8Pwm6uSNuCrNxpNX+nDira20qqyZriv384xhoZ2IX6Lr/wg8eiwpXYGQqPEgjsBrX+er3X46B9ie2h6bX/4i6IfcLS2mIY++7TfoCuLj6hwgN89CwBJ3Gx8rOVsn0kQ48tDS/4X1L8SzMb3BbkGdtwsD05AbP9YOU8xui+uljZei/P/3qx2HQbsSO0Ar/lveHLZYCVK4GeN1P5N+eAb2R2Aya9ueUY+8dlhLtdhWvgfEfz4AeGf9hR2wcaeTPpacvRdzLTx8J/Qq1Ij995PrJ3/mp0p8LO5YyF0s215g/9yxoZ2IXaLo+FiwtgTlvIAznp4+G3khsBk3Pf5mYvqOwMqZnNnw3i9cRDoT2JPaCZuvreskDuXkHxfQMvfbBgdBbiI2hC/3QLCg6lESZZkrfJ7nSj3gWNEMcRkYJsf5L0BXENBl1WV8/aquMLM9/eSJ0UtxZmFGGg4MigZ75ovHnvsOcUX09zRbzjCrU/K/Rev0gT2i2vu4cZrNI1+sH2e49Yb3lRC6e9fSS9ETohjj7hdkx0rQ/57/rxIrhBQdCv494Hlrtz1lo30Qjz1z+cRh02ndTNIMm1j8NxtmePs+76i+/esJHrH/6dGh/YntoRf9l2RtQWgJXzY4bdtbqx0OhnYldoHv7I79jf4+rOGGaRWr14yToLcTG0Ky/v32rjAsdX9S5vgI4ENqT2Asa7Z/9vq0yStiGNNVHkOe/RGmqwBQ0jEDHFpoJ85BLN+jPaRoHGWl2/2yx3msxVRHjZ0Td4GfV/Bf9bCM1dBiBfrHeJ1HR6dKEedDf5yPdBLilp/efC3Ribb1VxlUHGQe8iTb0598+Hdqf2B6a3b9SOerezsoIBWaJ3IBn1fp0j4PeQmwMzc5/idLK3UZWRkmKX2zA4Vc/DoPeSGwGrdvfY24RpKmVjrLuCQdCuxE7Qkv64TZ3Z44dVPrxUOh3EK9C1/lHGpz7ZzpVsYTF7K8Hyj+Ogd5FbAk9uL9H9LIyYg821sjo13gYtD+xPbRU/9Ds/b1sZTTIr5OStMGHA6GdiV2gUf2UmQcfJdUbXOoe6HoN1A0b2uQD4EBosXJqQTwKnXvXAEX+oZlfGaS9ewetDGCLeV1exH4e9Mr+2TDpv2iggYFW+LdBsTbngv/yUroGH0jleEX7MOgtxMbQ7PzKoMuajKyM8pYIiB194HnQzsQu0Gp/LvpulVHylsgK/TgG2p/YHrqXf6T3bJUBhfxFWUDPg95CbAytm/+ycasM9WOxWu0PnTniLWE2iLRi/4Z3bJXRvY76s0+Arl93I3aMNOHPBce2PdSkL7Kpnwe9l9gGutCPoFgkUtytXAGbex0vKCznSOs00o+ToLcQG0N/QErCrG6jrTIynVUDwo896vCzssPrNnAYtD+xPfR9fwmKeu/CVhm5OAn1STPkvhxBW1KXjeP60Y/DoJ2JXaAL/RCPy+PXc80L9d7OV294dVPbC/fb4EDoLcTG0IV+TCDrrIxMHIFg512BWj9OgvYk9oK+9SMoZG55q/JMiGS40/sLjacu28T3MVfd0Aha+u3QJXr+0Q//MBtHutaPQOzh3H0RBoZS5OJ/MjqJ9x2vQb7qgSuvX0StH0+FLptzrvTDh9gl0oV+iAbi2lAsqNmbK3yBAyLNdbRzW8Y6Btqf2B76A2IURE2zZY16KGfZlwqofJSL4feABka+xCNX+ekx0LuILaFr/Zit7s4ZA7y5Qg2sDr/t4zDodxCvQt/6occctDKGriMPzH85D9qN2BG61g/QwcLSVDSGl78C6LePY6A3EptBf0BKcjWWH/GqyJqoxKnMufEVBLJxAJwJ7U9sDH3fX0CxnIB4orMyuilTyRh6g+0bfDgQ2p/YHrrQD+jV6qb/SdRrMit8TRWwyEabxgFwJrQnsQt0rR+LR3VqFNiGHRBv73ge9EZiM+haPyicuRcVmIxzAMTMl9wfi3sGtBuxF3ShH92C29yJTteU+ohbRrZk3Qe9hdgY+tYPnOqanA+mTN33ZGKE06HQbsQu0LV+MF8+94rU5dK8B8/KyJaI+6D9ie2hC/3Q12QnSnYLn5p9P/6d0D7EltAh5wz/Hv8exON/LUjHOuz5CksAAAAASUVORK5CYII=';

/**
 * Contains the cross cursor.
 */
ColorPicker.prototype.crossImage = 'data:image/gif;base64,R0lGODlhDwAPAKEBAAAAAP///////////yH5BAEKAAIALAAAAAAPAA8AAAIklB8Qx53b4otSUWcvyiz4/4AeQJbmKY4p1HHapBlwPL/uVRsFADs=';

/**
 * Contains the arrow cursor.
 */
ColorPicker.prototype.arrowImage = 'data:image/gif;base64,R0lGODlhBwALAKECAAAAAP///6g8eKg8eCH5BAEKAAIALAAAAAAHAAsAAAITTIQYcLnsgGxvijrxqdQq6DRJAQA7';

/**
 * Constructs a new color dialog.
 */
var ColorDialog = function(editorUi, color, apply, cancelFn, defaultColor, defaultColorValue, singleColorMode)
{
	var cssDefaultColor = (defaultColorValue != null) ?
		mxUtils.getLightDarkColor(defaultColorValue,
			null, null, singleColorMode) : null;
	var graph = editorUi.editor.graph;
	this.editorUi = editorUi;
	var useDefault = false;

	function validateColor(color)
	{
		return mxUtils.color2hex('#' + color).substring(1).toUpperCase();
	};

	// Converts color names to hex
	if (color != 'default' &&
		color != 'transparent' &&
		color != mxConstants.NONE &&
		!mxUtils.isVarColor(color) &&
		!mxUtils.isHexColor(color) &&
		!mxUtils.isRgbColor(color) &&
		!mxUtils.isLightDarkColor(color))
	{
		color = mxUtils.color2hex(color);
	}

	var input = document.createElement('input');
	input.style.textOverflow = 'ellipsis';
	input.style.margin = '0 0 0 4px';
	input.style.width = '100%';

	var wrapper = document.createElement('div');
	wrapper.style.padding = '0 4px';

	var inputDiv = document.createElement('div');
	inputDiv.style.display = 'flex';
	inputDiv.style.alignItems = 'center';
	wrapper.appendChild(inputDiv);
	
	var darkInput = document.createElement('input');
	darkInput.style.textOverflow = 'ellipsis';
	darkInput.style.margin = '0 4px';
	darkInput.style.maxWidth = '70px';
	darkInput.style.width = '100%';

	var darkSelect = document.createElement('select');
	darkSelect.style.textOverflow = 'ellipsis';
	darkSelect.style.width = '100%';

	var autoDarkOption = document.createElement('option');
	autoDarkOption.setAttribute('value', 'automatic');
	mxUtils.write(autoDarkOption, mxResources.get('automatic'));
	darkSelect.appendChild(autoDarkOption);

	var customDarkOption = document.createElement('option');
	customDarkOption.setAttribute('value', 'custom');
	mxUtils.write(customDarkOption, mxResources.get('userDefined'));
	darkSelect.appendChild(customDarkOption);
	darkSelect.value = (!mxUtils.isLightDarkColor(color) ?
		'automatic' : 'custom');

	var darkColorDiv = document.createElement('div');
	darkColorDiv.style.padding = '0 6px 0 6px';

	var img = document.createElement('img');
	img.setAttribute('title', mxResources.get('dark'));
	img.className = 'geAdaptiveAsset';
	img.src = Editor.thinDarkImage;
	img.style.width = '20px';
	darkColorDiv.appendChild(img);

	mxEvent.addListener(img, 'click', function()
	{
		darkInput.focus();
	});

	darkColorDiv.appendChild(darkInput);
	darkColorDiv.appendChild(darkSelect);
	darkColorDiv.style.display = 'flex';
	darkColorDiv.style.alignItems = 'center';

	var advancedDiv = document.createElement('div');
	advancedDiv.style.display = 'flex';
	advancedDiv.style.alignItems = 'center';
	advancedDiv.style.userSelect = 'none';
	advancedDiv.style.cursor = 'pointer';
	advancedDiv.style.padding = '2px 0 2px 0';
	
	var collapse = document.createElement('img');
	collapse.setAttribute('title', mxResources.get('dark'));
	collapse.className = 'geAdaptiveAsset';
	collapse.style.marginLeft = '-2px';
	advancedDiv.appendChild(collapse);

	mxUtils.write(advancedDiv, mxResources.get('advanced'));

	var swapBtn = document.createElement('img');
	swapBtn.setAttribute('title', mxResources.get('swap'));
	swapBtn.className = 'geAdaptiveAsset';
	swapBtn.style.marginLeft = '6px';
	swapBtn.style.width = '16px';
	swapBtn.style.height = '16px';
	swapBtn.style.cursor = 'pointer';
	swapBtn.src = Editor.swapImage;

	mxEvent.addListener(swapBtn, 'click', function(evt)
	{
		toggleDarkColor(true, false);
		darkSelect.value = 'custom';
		setDefaultSelected(false);
		
		var tmp = input.value;
		input.value = darkInput.value;
		darkInput.value = tmp;

		updateInputColors();
		currentInput.focus();
		mxEvent.consume(evt);
	});

	advancedDiv.appendChild(swapBtn);

	var copyBtn = swapBtn.cloneNode();
	copyBtn.setAttribute('title', mxResources.get('copy'));
	copyBtn.style.marginLeft = '2px';
	copyBtn.src = Editor.doubleArrowUpImage;

	mxEvent.addListener(copyBtn, 'click', function(evt)
	{
		toggleDarkColor(true, false);
		darkSelect.value = 'custom';
		setDefaultSelected(false);

		if (currentInput == darkInput)
		{
			input.value = darkInput.value;
		}
		else
		{
			darkInput.value = input.value;
		}
		
		updateInputColors();
		currentInput.focus();
		mxEvent.consume(evt);
	});

	advancedDiv.appendChild(copyBtn);

	var opacityBtn = copyBtn.cloneNode();
	opacityBtn.setAttribute('title', mxResources.get('opacity'));
	opacityBtn.src = Editor.opacityImage;

	mxEvent.addListener(opacityBtn, 'click', function(evt)
	{
		var rgb = mxUtils.parseColor('#' + currentInput.value);
		var opacity = String(Math.floor(Math.max(0, Math.min(rgb.a * 100, 100))));

		editorUi.prompt(mxResources.get('opacity') + ' (0-100)', opacity, function(newValue)
		{
			if (!isNaN(newValue) && newValue !== '' &&
				newValue >= 0 && newValue <= 100)
			{
				setDefaultSelected(false);
				
				input.value = mxUtils.rgba2hex(mxUtils.addAlphaToColor(
					'#' + input.value, newValue / 100, true)).
						substring(1).toUpperCase();
				darkInput.value = mxUtils.rgba2hex(mxUtils.addAlphaToColor(
					'#' + darkInput.value, newValue / 100, true)).
						substring(1).toUpperCase();
				
				updateInputColors();
				currentInput.focus();
			}
		});
		
		mxEvent.consume(evt);
	});

	advancedDiv.appendChild(opacityBtn);

	function updateCollapseIcon()
	{
		if (darkColorDiv.style.display == 'none')
		{
			collapse.src = Editor.arrowRightImage;
		}
		else
		{
			collapse.src = Editor.arrowDownImage;
		}
	};

	updateCollapseIcon();

	function toggleDarkColor(show, transferFocus)
	{
		if (show || darkColorDiv.style.display == 'none')
		{
			darkColorDiv.style.display = 'flex';

			if (transferFocus !== false)
			{
				darkInput.focus();
			}
		}
		else
		{
			darkColorDiv.style.display = 'none';
		}

		updateCollapseIcon();
	};
	
	mxEvent.addListener(advancedDiv, 'click', function()
	{
		toggleDarkColor();
		updateCollapseIcon();
		ColorDialog.collapsed = darkColorDiv.style.display == 'none';
	});

	function isDefaultColor(color)
	{
		var lc = color.toLowerCase();

		return defaultColor != null && (color == '' ||
			lc == 'automatic' || lc == 'default' ||
			lc == mxResources.get('default').toLowerCase() ||
			lc == mxResources.get('automatic').toLowerCase());
	};

	function updateInputColor(input)
	{
		if (input.value == '' && cssDefaultColor != null && useDefault)
		{
			input.style.background = (input == darkInput) ?
				cssDefaultColor.dark :
				cssDefaultColor.light;
		}
		else if (input.value == '' || input.value == mxConstants.NONE ||
			mxUtils.isVarColor(input.value))
		{
			input.style.background = 'transparent';
		}
		else
		{
			var color = '#' + input.value;
			input.style.background = color;
		}

		if (input.style.background == 'transparent')
		{
			input.style.color = 'light-dark(#000000, #ffffff)';
		}
		else
		{
			input.style.color =
				(mxUtils.isDarkColor(input.style.background)) ?
				'#ffffff' : '#000000';
		}
	};

	function updateInputColors()
	{
		updateInputColor(input);
		updateInputColor(darkInput);
	};

	function setDefaultSelected(selected)
	{
		if (useDefault != selected)
		{
			if (selected)
			{
				if (singleColorMode && cssDefaultColor != null)
				{
					input.value = cssDefaultColor.light.substring(1).toUpperCase();
					selected = false;
				}
				else
				{
					input.value = '';
					darkInput.value = '';
					darkSelect.value = 'automatic';
					input.setAttribute('placeholder', mxResources.get('useBlackAndWhite'));
					darkInput.setAttribute('title', mxResources.get('useBlackAndWhite'));
					input.setAttribute('title', mxResources.get('useBlackAndWhite'));
				}
			}
			else
			{
				if (input.value == '' && cssDefaultColor != null)
				{	
					input.value = cssDefaultColor.light.substring(1).toUpperCase();
				}

				input.removeAttribute('placeholder');
				input.removeAttribute('title');

				if (darkInput.value == '' && cssDefaultColor != null)
				{
					darkInput.style.background = cssDefaultColor.dark;
					var style = mxUtils.getCurrentStyle(darkInput);
					darkInput.value = mxUtils.rgba2hex(style.backgroundColor).
						substring(1).toUpperCase();
				}

				darkInput.removeAttribute('placeholder');
				darkInput.removeAttribute('title');
				selected = false;
			}
		}

		useDefault = selected;
	};

	var defaultBtn = mxUtils.button('', function()
	{
		setDefaultSelected(true);
		updateInputColors();
		currentInput.focus();

		if (currentInput.value == '' && cssDefaultColor != null && useDefault)
		{
			picker.fromString((currentInput == darkInput) ?
				cssDefaultColor.dark :
				cssDefaultColor.light, true);
		}
	});

	mxEvent.addListener(darkSelect, 'change', function(evt)
	{
		if (darkSelect.value == 'automatic')
		{
			input.focus();
			inputChanged(input);
		}
		else
		{
			setDefaultSelected(false);
			darkInput.focus();
			selectInput();
		}

		updateInputColors();
		mxEvent.consume(evt);
	});

	function inputChanged(elt)
	{
		if (elt.value.charAt(0) == '#')
		{
			elt.value = elt.value.substring(1);
		}

		if (isDefaultColor(elt.value) && cssDefaultColor != null)
		{
			defaultBtn.click();
		}
		else
		{
			if (elt.value != mxConstants.NONE &&
				!mxUtils.isVarColor(elt.value))
			{
				elt.value = validateColor(elt.value);
				picker.fromString('#' + elt.value);
			}

			setDefaultSelected(false);

			if (elt == darkInput &&
				(input.value != mxConstants.NONE ||
				darkInput.value != mxConstants.NONE))
			{
				darkSelect.value = 'custom';
			}

			if (elt == input && darkSelect.value == 'automatic')
			{
				if (input.value == mxConstants.NONE)
				{
					darkInput.value = mxConstants.NONE;
				}
				else if (mxUtils.isVarColor(input.value))
				{
					darkInput.value = input.value;
				}
				else
				{
					darkInput.value = mxUtils.rgba2hex(mxUtils.
						getInverseColor('#' + elt.value)).
						substring(1).toUpperCase();
				}
			}

			updateInputColors();
		}
	};

	mxEvent.addListener(input, 'change', function()
	{
		inputChanged(input);
	});

	mxEvent.addListener(darkInput, 'change', function()
	{
		inputChanged(darkInput);
	});
	
	var applyFunction = (apply != null) ? apply :
		this.createApplyFunction();

	function doApply()
	{
		var color = input.value;
		var dark = darkInput.value;
		
		// Blocks any non-alphabetic chars in colors
		if ((useDefault && cssDefaultColor != null) ||
			((/(^#?[a-zA-Z0-9]*$)/.test(color) || mxUtils.isVarColor(color)) &&
			(/(^#?[a-zA-Z0-9]*$)/.test(dark) || mxUtils.isVarColor(dark))))
		{
			if (useDefault)
			{
				color = 'default';
			}
			else
			{
				if (color == '')
				{
					color = mxConstants.NONE;
				}

				if (dark == '')
				{
					dark = mxConstants.NONE;
				}

				if (color != mxConstants.NONE &&
					!mxUtils.isVarColor(color) &&
					color.charAt(0) != '#')
				{
					color = '#' + validateColor(color);
				}

				if (dark != mxConstants.NONE &&
					!mxUtils.isVarColor(dark) &&
					dark.charAt(0) != '#')
				{
					dark = '#' + validateColor(dark);
				}

				var recentCount = 12;

				if (color == mxConstants.NONE ||
					dark == mxConstants.NONE)
				{
					ColorDialog.addRecentColor(mxConstants.NONE, recentCount);
				}

				if (darkSelect.value != 'automatic' &&
					dark != mxConstants.NONE)
				{
					if (color == mxConstants.NONE)
					{
						ColorDialog.addRecentColor(dark.substring(1), recentCount);
					}
					else
					{
						var temp = 'light-dark(' + color + ',' + dark + ')';
						ColorDialog.addRecentColor(temp, recentCount);
					}
				}
				else if (color != mxConstants.NONE)
				{
					ColorDialog.addRecentColor(color.substring(1), recentCount);
				}
				
				if (color != 'none' || dark != 'none')
				{
					color = (color == mxConstants.NONE) ? 'transparent' : color;
					dark = (dark == mxConstants.NONE) ? 'transparent' : dark;

					if (!singleColorMode && darkSelect.value != 'automatic')
					{
						color = 'light-dark(' + color + ',' + dark + ')';
					}
				}
			}
			
			applyFunction(color);
			editorUi.hideDialog();
		}
		else
		{
			editorUi.handleError({message: mxResources.get('invalidInput')});	
		}
	};

	function selectInput()
	{
		if (!mxClient.IS_TOUCH)
		{
			window.setTimeout(function()
			{
				currentInput.focus();
				
				if (mxClient.IS_GC || mxClient.IS_FF || document.documentMode >= 5)
				{
					currentInput.select();
				}
				else
				{
					document.execCommand('selectAll', false, null);
				}
			}, 0);
		}
	};
	
	this.init = function()
	{
		selectInput();
	};

	var picker = new ColorPicker();
	var currentInput = (Editor.isDarkMode() &&
		(graph.getAdaptiveColors() == 'auto' ||
		(graph.getAdaptiveColors() == 'simple' &&
		darkSelect.value == 'custom')) &&
		!singleColorMode) ? darkInput : input;
	
	function updatePickerFromCurrentInput()
	{
		if (currentInput.value != mxConstants.NONE &&
			currentInput.value != '')
		{
			picker.fromString('#' + currentInput.value, true);
		}
	};

	mxEvent.addListener(input, 'focus', function()
	{
		currentInput = input;
		updatePickerFromCurrentInput();
		copyBtn.src = Editor.doubleArrowDownImage;
	});

	mxEvent.addListener(darkInput, 'focus', function()
	{
		currentInput = darkInput;
		updatePickerFromCurrentInput();
		copyBtn.src = Editor.doubleArrowUpImage;
	});

	picker.addListener('change', function(sender, evt)
	{
		var color = evt.getProperty('color').substring(1).toUpperCase();

		if (currentInput.value.toUpperCase() != color)
		{
			currentInput.value = color;
			inputChanged(currentInput);

			window.setTimeout(function()
			{
				currentInput.focus();
			}, 0);
		}
	});

	var div = document.createElement('div');
	div.appendChild(picker.div);

	var center = document.createElement('div');
	center.style.display = 'flex';
	center.style.flexFlow = 'row wrap';
	center.style.justifyContent = 'center';
	center.style.paddingTop = '10px';
	
	function createRecentColorTable()
	{
		var table = addPresets((ColorDialog.recentColors.length == 0) ?
			['FFFFFF'] : ColorDialog.recentColors, 11, 'FFFFFF', true);
		table.style.marginBottom = '8px';
		
		return table;
	};
	
	var addPresets = mxUtils.bind(this, function(presets, rowLength, defaultColor, addResetOption)
	{
		rowLength = (rowLength != null) ? rowLength : 12;
		var table = document.createElement('table');
		table.style.borderCollapse = 'collapse';
		table.setAttribute('cellspacing', '0');
		table.style.marginBottom = '20px';
		table.style.cellSpacing = '0px';
		table.style.marginLeft = '1px';
		var tbody = document.createElement('tbody');
		table.appendChild(tbody);

		var rows = presets.length / rowLength;
		
		for (var row = 0; row < rows; row++)
		{
			var tr = document.createElement('tr');
			
			for (var i = 0; i < rowLength; i++)
			{
				(mxUtils.bind(this, function(clr)
				{
					var td = document.createElement('td');
					td.style.border = '0px solid black';
					td.style.padding = '0px';
					td.style.width = '16px';
					td.style.height = '16px';
					
					if (clr == null)
					{
						clr = defaultColor;
					}

					if (clr != null)
					{
						td.style.borderWidth = '1px';

						if (clr == mxConstants.NONE || clr == null)
						{
							td.style.background = 'url(\'' + Dialog.prototype.noColorImage + '\')';
						}
						else
						{
							if (mxUtils.isLightDarkColor(clr))
							{
								var cssColor = mxUtils.getLightDarkColor(clr);
								td.style.background = 'linear-gradient(to right bottom, ' +
									cssColor.cssText + ' 50%, ' +
									mxUtils.invertLightDarkColor(cssColor).cssText + ' 50.3%)';
							}
							else
							{
								td.style.backgroundColor = '#' + clr;
							}
						}

						var name = this.colorNames[String(clr).toUpperCase()];

						if (name != null)
						{
							td.setAttribute('title', name);
						}
					}
					
					tr.appendChild(td);

					if (clr != null)
					{
						td.style.cursor = 'pointer';
						
						mxEvent.addListener(td, 'click', function()
						{
							if (clr == mxConstants.NONE)
							{
								if (currentInput == darkInput &&
									darkInput.value == mxConstants.NONE)
								{
									input.value = mxConstants.NONE
								}
								else
								{
									currentInput.value = mxConstants.NONE;
								}
								
								if (currentInput == input ||
									(currentInput == darkInput &&
									input.value == mxConstants.NONE))
								{
									darkSelect.value = 'automatic';
								}

								inputChanged(currentInput);

								window.setTimeout(function()
								{
									currentInput.focus();
								}, 0);
							}
							else
							{
								if (mxUtils.isLightDarkColor(clr))
								{
									toggleDarkColor(true);
									darkSelect.value = 'custom';
									var cssColor = mxUtils.getLightDarkColor(clr);
									input.value = cssColor.light.substring(1).toUpperCase();
									darkInput.value = cssColor.dark.substring(1).toUpperCase();
									inputChanged(input);
									inputChanged(darkInput);
									
									if (currentInput == input)
									{
										picker.fromString(cssColor.light);
									}
									else
									{
										picker.fromString(cssColor.dark);
									}	
								}
								else
								{
									picker.fromString('#' + clr);
								}
							}
						});
						
						mxEvent.addListener(td, 'dblclick', doApply);
					}
				}))(presets[row * rowLength + i]);
			}
			
			tbody.appendChild(tr);
		}
		
		if (addResetOption)
		{
			var td = document.createElement('td');
			td.setAttribute('title', mxResources.get('reset'));
			td.className = 'geAdaptiveAsset';
			td.style.border = '1px solid black';
			td.style.padding = '0px';
			td.style.width = '16px';
			td.style.height = '16px';
			td.style.backgroundImage = 'url(\'' + Editor.crossImage + '\')';
			td.style.backgroundPosition = 'center center';
			td.style.backgroundSize = '14px 14px';
			td.style.backgroundRepeat = 'no-repeat';
			td.style.cursor = 'pointer';
			
			tr.appendChild(td);

			mxEvent.addListener(td, 'click', function()
			{
				ColorDialog.resetRecentColors();
				table.parentNode.replaceChild(createRecentColorTable(), table);
			});
		}
		
		center.appendChild(table);
		
		return table;
	});

	if (!singleColorMode)
	{
		wrapper.appendChild(advancedDiv);

		if (ColorDialog.collapsed && (!Editor.isDarkMode() ||
			graph.getAdaptiveColors() != 'auto') &&
			darkSelect.value == 'automatic')
		{
			darkColorDiv.style.display = 'none';
			updateCollapseIcon();
		}
		
		var img = document.createElement('img');
		img.setAttribute('title', mxResources.get('light'));
		img.className = 'geAdaptiveAsset';
		img.src = Editor.thinLightImage;
		img.style.width = '20px';
		inputDiv.appendChild(img);

		mxEvent.addListener(img, 'click', function()
		{
			input.focus();
		});
	}

	inputDiv.appendChild(input);

	if (cssDefaultColor != null)
	{
		defaultBtn.setAttribute('title', mxResources.get(
			(singleColorMode) ? 'default' : 'useBlackAndWhite'));
		defaultBtn.style.cursor = 'pointer';
		defaultBtn.style.position = 'relative';
		defaultBtn.style.marginLeft = '4px';
		defaultBtn.innerText = '';

		var def = document.createElement('div');
		def.style.display = 'inline-block';
		def.style.verticalAlign = 'middle';
		def.style.borderStyle = 'solid';
		def.style.borderWidth = '1px';
		def.style.marginTop = '-2px';
		def.style.width = '12px';
		def.style.height = '12px';

		if (singleColorMode)
		{
			def.style.backgroundColor = cssDefaultColor.light;
		}
		else
		{
			def.style.backgroundImage = 'linear-gradient(to right bottom, ' +
				cssDefaultColor.cssText + ' 50%, ' +
				mxUtils.invertLightDarkColor(cssDefaultColor).cssText + ' 50.3%)';
		}

		defaultBtn.appendChild(def);
		inputDiv.appendChild(defaultBtn);

		mxEvent.addListener(defaultBtn, 'dblclick', doApply);
	}

	var clrInput = document.createElement('input');
	clrInput.setAttribute('type', 'color');
	clrInput.style.position = 'relative';
	clrInput.style.visibility = 'hidden';
	clrInput.style.top = '10px';
	clrInput.style.width = '0px';
	clrInput.style.height = '0px';
	clrInput.style.border = 'none';
	
	div.style.whiteSpace = 'nowrap';
	inputDiv.appendChild(clrInput);

	var dropperBtn = mxUtils.button('', function()
	{
		// LATER: Check if clrInput is expanded
		if (document.activeElement == clrInput)
		{
			input.focus();
		}
		else
		{
			clrInput.value = '#' + input.value;
			clrInput.click();
		}
	});

	dropperBtn.style.cursor = 'pointer';

	var dropper = document.createElement('img');
	dropper.src = Editor.colorDropperImage;
	dropper.className = 'geAdaptiveAsset';
	dropper.style.position = 'relative';
	dropper.style.verticalAlign = 'middle';
	dropper.style.marginTop = '-2px';
	dropper.style.width = 'auto';
	dropper.style.height = '14px';

	dropperBtn.appendChild(dropper);
	inputDiv.appendChild(dropperBtn);

	div.appendChild(wrapper);

	mxEvent.addListener(clrInput, 'change', function()
	{
		picker.fromString(clrInput.value);
	});

	if (!singleColorMode)
	{
		div.appendChild(darkColorDiv);
	}

	// Adds recent colors
	createRecentColorTable();
		
	// Adds presets
	var table = addPresets(this.presetColors);
	table.style.marginBottom = '8px';
	table = addPresets(this.defaultColors);
	table.style.marginBottom = '16px';

	div.appendChild(center);

	var buttons = document.createElement('div');
	buttons.style.display = 'flex';
	buttons.style.whiteSpace = 'nowrap';
	buttons.style.alignItems = 'center';
	buttons.style.justifyContent = 'end';

	if (!editorUi.isOffline())
	{
		buttons.appendChild(editorUi.createHelpIcon('https://github.com/jgraph/drawio/discussions/4713'));
	}
	
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
		buttons.appendChild(cancelBtn);
	}
	
	var applyBtn = mxUtils.button(mxResources.get('apply'), doApply);
	applyBtn.className = 'geBtn gePrimaryBtn';
	buttons.appendChild(applyBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}
	
	if (color != null)
	{
		if (color == 'default')
		{
			defaultBtn.click();
		}
		else
		{
			var cssColor = mxUtils.getLightDarkColor(color);

			if (color == mxConstants.NONE ||
				cssColor.light == 'transparent')
			{
				input.value = mxConstants.NONE;
			}
			else
			{
				if (mxUtils.isVarColor(cssColor.light))
				{
					input.value = cssColor.light;
				}
				else
				{
					if (mxUtils.isRgbColor(cssColor.light))
					{
						cssColor.light = mxUtils.rgba2hex(cssColor.light);
					}
					else if (!mxUtils.isHexColor(cssColor.light))
					{
						cssColor.light = '#' + validateColor(cssColor.light);
					}

					input.value = cssColor.light.substring(1).toUpperCase();
				}
			}

			if (color == mxConstants.NONE ||
				cssColor.dark == 'transparent')
			{
				darkInput.value = mxConstants.NONE;
			}
			else
			{
				if (mxUtils.isVarColor(cssColor.dark))
				{
					darkInput.value = cssColor.dark;
				}
				else
				{
					if (mxUtils.isRgbColor(cssColor.dark))
					{
						cssColor.dark = mxUtils.rgba2hex(cssColor.dark);
					}
					else if (!mxUtils.isHexColor(cssColor.dark))
					{
						cssColor.dark = '#' + validateColor(cssColor.dark);
					}
					
					darkInput.value = cssColor.dark.substring(1).toUpperCase();
				}
			}

			updatePickerFromCurrentInput();
			updateInputColors();
		}
	}
	
	if (!singleColorMode && ColorDialog.collapsed &&
		darkSelect.value == 'automatic' &&
		!Editor.isDarkMode())
	{
		darkColorDiv.style.display = 'none';
		updateCollapseIcon();
	}
	
	div.appendChild(buttons);
	this.picker = picker;
	this.colorInput = input;

	function keyPressed(e)
	{
		if (e.keyCode == 13)
		{
			inputChanged(mxEvent.getSource(e));
			doApply();
		}
	};

	mxEvent.addListener(input, 'keypress', keyPressed);
	mxEvent.addListener(darkInput, 'keypress', keyPressed);

	// LATER: Only fires if input if focused, should always
	// fire if this dialog is showing.
	mxEvent.addListener(div, 'keydown', function(e)
	{
		if (e.keyCode == 27)
		{
			editorUi.hideDialog();
			
			if (cancelFn != null)
			{
				cancelFn();
			}
			
			mxEvent.consume(e);
		}
	});
	
	this.container = div;
};

/**
 * Creates function to apply value
 */
ColorDialog.prototype.presetColors = ['E6D0DE', 'CDA2BE', 'B5739D', 'E1D5E7', 'C3ABD0', 'A680B8', 'D4E1F5', 'A9C4EB', '7EA6E0', 'D5E8D4', '9AC7BF', '67AB9F', 'D5E8D4', 'B9E0A5', '97D077', 'FFF2CC', 'FFE599', 'FFD966', 'FFF4C3', 'FFCE9F', 'FFB570', 'F8CECC', 'F19C99', 'EA6B66']; 

/**
 * Creates function to apply value
 */
ColorDialog.prototype.colorNames = {};

/**
 * Creates function to apply value
 */
ColorDialog.prototype.defaultColors = ['none', 'FFFFFF', 'E6E6E6', 'CCCCCC', 'B3B3B3', '999999', '808080', '666666', '4D4D4D', '333333', '1A1A1A', '000000', 'FFCCCC', 'FFE6CC', 'FFFFCC', 'E6FFCC', 'CCFFCC', 'CCFFE6', 'CCFFFF', 'CCE5FF', 'CCCCFF', 'E5CCFF', 'FFCCFF', 'FFCCE6',
		'FF9999', 'FFCC99', 'FFFF99', 'CCFF99', '99FF99', '99FFCC', '99FFFF', '99CCFF', '9999FF', 'CC99FF', 'FF99FF', 'FF99CC', 'FF6666', 'FFB366', 'FFFF66', 'B3FF66', '66FF66', '66FFB3', '66FFFF', '66B2FF', '6666FF', 'B266FF', 'FF66FF', 'FF66B3', 'FF3333', 'FF9933', 'FFFF33',
		'99FF33', '33FF33', '33FF99', '33FFFF', '3399FF', '3333FF', '9933FF', 'FF33FF', 'FF3399', 'FF0000', 'FF8000', 'FFFF00', '80FF00', '00FF00', '00FF80', '00FFFF', '007FFF', '0000FF', '7F00FF', 'FF00FF', 'FF0080', 'CC0000', 'CC6600', 'CCCC00', '66CC00', '00CC00', '00CC66',
		'00CCCC', '0066CC', '0000CC', '6600CC', 'CC00CC', 'CC0066', '990000', '994C00', '999900', '4D9900', '009900', '00994D', '009999', '004C99', '000099', '4C0099', '990099', '99004D', '660000', '663300', '666600', '336600', '006600', '006633', '006666', '003366', '000066',
		'330066', '660066', '660033', '330000', '331A00', '333300', '1A3300', '003300', '00331A', '003333', '001933', '000033', '190033', '330033', '33001A'];

/**
 * Creates function to apply value
 */
ColorDialog.prototype.createApplyFunction = function()
{
	return ColorDialog.createApplyFunction(this.editorUi, this.currentColorKey);
};

/**
 * Creates function to apply value
 */
ColorDialog.createApplyFunction = function(editorUi, colorKey)
{
	return function(color)
	{
		var graph = editorUi.editor.graph;
		
		graph.getModel().beginUpdate();
		try
		{
			graph.setCellStyles(colorKey, color);
			editorUi.fireEvent(new mxEventObject('styleChanged', 'keys', [colorKey],
				'values', [color], 'cells', graph.getSelectionCells()));
		}
		finally
		{
			graph.getModel().endUpdate();
		}
	};
};

/**
 * 
 */
ColorDialog.recentColors = [];

/**
 * Adds recent color for later use.
 */
ColorDialog.addRecentColor = function(color, max)
{
	if (color != null)
	{
		mxUtils.remove(color, ColorDialog.recentColors);
		ColorDialog.recentColors.splice(0, 0, color);
		
		if (ColorDialog.recentColors.length >= max)
		{
			ColorDialog.recentColors.pop();
		}
	}
};

/**
 * Adds recent color for later use.
 */
ColorDialog.resetRecentColors = function()
{
	ColorDialog.recentColors = [];
};

/**
 * Remembers collapsed dark color state.
 */
ColorDialog.collapsed = true;

/**
 * Constructs a new about dialog.
 */
var AboutDialog = function(editorUi)
{
	var div = document.createElement('div');
	div.setAttribute('align', 'center');
	var h3 = document.createElement('h3');
	mxUtils.write(h3, mxResources.get('about') + ' GraphEditor');
	div.appendChild(h3);
	var img = document.createElement('img');
	img.style.border = '0px';
	img.setAttribute('width', '176');
	img.setAttribute('width', '151');
	img.setAttribute('src', IMAGE_PATH + '/logo.png');
	div.appendChild(img);
	mxUtils.br(div);
	mxUtils.write(div, 'Powered by mxGraph ' + mxClient.VERSION);
	mxUtils.br(div);
	var link = document.createElement('a');
	link.setAttribute('href', 'http://www.jgraph.com/');
	link.setAttribute('target', '_blank');
	mxUtils.write(link, 'www.jgraph.com');
	div.appendChild(link);
	mxUtils.br(div);
	mxUtils.br(div);
	var closeBtn = mxUtils.button(mxResources.get('close'), function()
	{
		editorUi.hideDialog();
	});
	closeBtn.className = 'geBtn gePrimaryBtn';
	div.appendChild(closeBtn);
	
	this.container = div;
};

/**
 * Constructs a simple textarea dialog.
 */
var SimpleTextareaDialog = function(editorUi, value, fn, buttonLabel, helpLink)
{
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
	textarea.value = (value != null) ? value : '';
	div.appendChild(textarea);

	var buttons = document.createElement('div');
	buttons.style.position = 'absolute';
	buttons.style.bottom = '46px';
	buttons.style.right = '30px';
	buttons.style.left = '30px';
	buttons.style.justifyContent = 'end';
	buttons.style.display = 'flex';

	if (helpLink != null && !editorUi.isOffline())
	{
		buttons.appendChild(editorUi.createHelpIcon(helpLink));
	}

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});

	cancelBtn.setAttribute('title', 'Escape');
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}
	
	var okBtn = mxUtils.button((buttonLabel != null) ?
		buttonLabel : mxResources.get('apply'), function()
	{
		editorUi.hideDialog();
		fn(textarea.value);
	});
	
	okBtn.setAttribute('title', 'Ctrl+Enter');
	okBtn.className = 'geBtn gePrimaryBtn';
	buttons.appendChild(okBtn);

	mxEvent.addListener(textarea, 'keydown', function(e)
	{
		if (e.keyCode == 13 && mxEvent.isControlDown(e))
		{
			okBtn.click();
		}
	});
	
	if (!editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}

	this.init = function()
	{
		textarea.focus();
		textarea.scrollTop = 0;
	};

	div.appendChild(buttons);
	this.container = div;
};

/**
 * Constructs a new textarea dialog.
 */
var TextareaDialog = function(editorUi, title, url, fn, cancelFn, cancelTitle, w, h,
	addButtons, noHide, noWrap, applyTitle, helpLink, customButtons, header)
{
	w = (w != null) ? w : 300;
	h = (h != null) ? h : 120;
	noHide = (noHide != null) ? noHide : false;

	var div = document.createElement('div');
	div.style.position = 'absolute';
	div.style.top = '20px';
	div.style.bottom = '20px';
	div.style.left = '20px';
	div.style.right = '20px';

	var top = document.createElement('div');

	top.style.position = 'absolute';
	top.style.left = '0px';
	top.style.right = '0px';

	var main = top.cloneNode(false);
	var buttons = top.cloneNode(false);

	top.style.top = '0px';
	top.style.height = '20px';
	main.style.top = '20px';
	main.style.bottom = '64px';

	buttons.style.display = 'flex';
	buttons.style.whiteSpace = 'nowrap';
	buttons.style.alignItems = 'center';
	buttons.style.justifyContent = 'end';
	buttons.style.bottom = '10px';
	buttons.style.height = '50px';
	buttons.style.textAlign = 'right';
	buttons.style.paddingTop = '14px';
	buttons.style.boxSizing = 'border-box';

	mxUtils.write(top, title);

	div.appendChild(top);
	div.appendChild(main);
	div.appendChild(buttons);

	if (header != null)
	{
		top.appendChild(header);
	}
	
	var nameInput = document.createElement('textarea');
	
	if (noWrap)
	{
		nameInput.setAttribute('wrap', 'off');
	}
	else
	{
		nameInput.style.wordBreak = 'break-all';
	}
	
	nameInput.setAttribute('spellcheck', 'false');
	nameInput.setAttribute('autocorrect', 'off');
	nameInput.setAttribute('autocomplete', 'off');
	nameInput.setAttribute('autocapitalize', 'off');
	
	mxUtils.write(nameInput, url || '');
	nameInput.style.resize = 'none';
	nameInput.style.outline = 'none';
	nameInput.style.position = 'absolute';
	nameInput.style.boxSizing = 'border-box';
	nameInput.style.top = '0px';
	nameInput.style.left = '0px';
	nameInput.style.height = '100%';
	nameInput.style.width = '100%';
	
	this.textarea = nameInput;

	this.init = function()
	{
		nameInput.focus();
		nameInput.scrollTop = 0;
	};

	main.appendChild(nameInput);

	if (helpLink != null && !editorUi.isOffline())
	{
		buttons.appendChild(editorUi.createHelpIcon(helpLink));
	}

	if (customButtons != null)
	{
		for (var i = 0; i < customButtons.length; i++)
		{
			(function(label, fn, title)
			{
				var customBtn = mxUtils.button(label, function(e)
				{
					fn(e, nameInput);
				});

				if (title != null)
				{
					customBtn.setAttribute('title', title);
				}

				customBtn.className = 'geBtn';
				
				buttons.appendChild(customBtn);
			})(customButtons[i][0], customButtons[i][1], customButtons[i][2]);
		}
	}
	
	var cancelBtn = mxUtils.button(cancelTitle || mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
		
		if (cancelFn != null)
		{
			cancelFn();
		}
	});

	cancelBtn.setAttribute('title', 'Escape');
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}
	
	if (addButtons != null)
	{
		addButtons(buttons, nameInput);
	}
	
	if (fn != null)
	{
		var genericBtn = mxUtils.button(applyTitle || mxResources.get('apply'), function()
		{
			if (!noHide)
			{
				editorUi.hideDialog();
			}
			
			fn(nameInput.value);
		});
		
		genericBtn.setAttribute('title', 'Ctrl+Enter');
		genericBtn.className = 'geBtn gePrimaryBtn';
		buttons.appendChild(genericBtn);

		mxEvent.addListener(nameInput, 'keydown', function(e)
		{
			if (e.keyCode == 13 && mxEvent.isControlDown(e))
			{
				genericBtn.click();
			}
		});
	}
	
	if (!editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}

	this.container = div;
};

/**
 * Constructs a new edit file dialog.
 */
var EditDiagramDialog = function(editorUi)
{
	var div = document.createElement('div');
	div.style.position = 'absolute';
	div.style.inset = '10px';

	var textarea = document.createElement('textarea');
	textarea.setAttribute('wrap', 'off');
	textarea.setAttribute('spellcheck', 'false');
	textarea.setAttribute('autocorrect', 'off');
	textarea.setAttribute('autocomplete', 'off');
	textarea.setAttribute('autocapitalize', 'off');
	textarea.style.overflow = 'auto';
	textarea.style.resize = 'none';
	textarea.style.position = 'absolute';
	textarea.style.top = '20px';
	textarea.style.bottom = '76px';
	textarea.style.left = '20px';
	textarea.style.right = '20px';

	var snapshot = editorUi.getDiagramSnapshot();
	textarea.value = mxUtils.getPrettyXml(snapshot.node);
	div.appendChild(textarea);
	
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
				
				reader.onload = function(e)
				{
					textarea.value = e.target.result;
				};
				
				reader.readAsText(file);
    		}
		    else
		    {
		    	textarea.value = editorUi.extractGraphModelFromEvent(evt);
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
	
	var buttons = document.createElement('div');
	buttons.style.display = 'flex';
	buttons.style.whiteSpace = 'nowrap';
	buttons.style.alignItems = 'center';
	buttons.style.justifyContent = 'end';
	buttons.style.position = 'absolute';
	buttons.style.height = '36px';
	buttons.style.bottom = '24px';
	buttons.style.left = '20px';
	buttons.style.right = '20px';

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});

	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}
	
	var select = document.createElement('select');
	select.style.textOverflow = 'ellipsis';
	select.style.width = '196px';
	select.className = 'geBtn';

	if (editorUi.editor.graph.isEnabled())
	{
		var applyOption = document.createElement('option');
		applyOption.setAttribute('value', 'apply');
		mxUtils.write(applyOption, mxResources.get('apply',
			null, 'Update Existing Drawing'));
		select.appendChild(applyOption);
	}

	if (editorUi.editor.graph.isEnabled())
	{
		var insertOption = document.createElement('option');
		insertOption.setAttribute('value', 'insert');
		mxUtils.write(insertOption, mxResources.get('insert'));
		select.appendChild(insertOption);
	}

	var newOption = document.createElement('option');
	newOption.setAttribute('value', 'new');
	mxUtils.write(newOption, mxResources.get('openInNewWindow'));
	
	if (EditDiagramDialog.showNewWindowOption && !navigator.standalone)
	{
		select.appendChild(newOption);
	}
	
	buttons.appendChild(select);

	var okBtn = mxUtils.button(mxResources.get('ok'), function()
	{
		// Removes all illegal control characters before parsing
		var data = Graph.zapGremlins(mxUtils.trim(textarea.value));
		var error = null;
		
		if (select.value == 'new')
		{
			editorUi.hideDialog();
			editorUi.editor.editAsNew(data);
		}
		else if (select.value == 'apply')
		{
			try
			{
				var node = mxUtils.parseXml(data).documentElement;
				var cause = Editor.extractParserError(node, mxResources.get('unknownError'));

				if (cause)
				{
					EditorUi.debug('EditDiagramDialogParserError', [this],
						'node', [node], 'cause', [cause]);
					error = mxResources.get('invalidInput') + ' (' + cause + ')';
				}
				else
				{
					if (node.nodeName == 'mxfile')
					{
						editorUi.editor.setGraphXml(node);
					}
					else
					{
						editorUi.updateDiagramData(snapshot, node);
					}
					
					editorUi.hideDialog();
				}
			}
			catch (e)
			{
				error = e;
			}
		}
		else if (select.value == 'insert')
		{
			editorUi.editor.graph.model.beginUpdate();
			try
			{
				var doc = mxUtils.parseXml(data);
				var model = new mxGraphModel();
				var codec = new mxCodec(doc);
				codec.decode(doc.documentElement, model);
				
				var children = model.getChildren(model.getChildAt(model.getRoot(), 0));
				editorUi.editor.graph.setSelectionCells(editorUi.editor.graph.importCells(children));
				
				// LATER: Why is hideDialog between begin-/endUpdate faster?
				editorUi.hideDialog();
			}
			catch (e)
			{
				error = e;
			}
			finally
			{
				editorUi.editor.graph.model.endUpdate();				
			}
		}
			
		if (error != null)
		{
			editorUi.handleError(error);
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
 * 
 */
EditDiagramDialog.showNewWindowOption = true;

/**
 * Constructs a new export dialog.
 */
var ExportDialog = function(editorUi)
{
	var graph = editorUi.editor.graph;
	var bounds = graph.getGraphBounds();
	var scale = graph.view.scale;
	
	var width = Math.ceil(bounds.width / scale);
	var height = Math.ceil(bounds.height / scale);

	var row, td;
	
	var table = document.createElement('table');
	var tbody = document.createElement('tbody');
	table.setAttribute('cellpadding', (mxClient.IS_SF) ? '0' : '2');
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.fontSize = '10pt';
	td.style.width = '100px';
	mxUtils.write(td, mxResources.get('filename') + ':');
	
	row.appendChild(td);
	
	var nameInput = document.createElement('input');
	nameInput.setAttribute('value', editorUi.editor.getOrCreateFilename());
	nameInput.style.width = '180px';

	td = document.createElement('td');
	td.appendChild(nameInput);
	row.appendChild(td);
	
	tbody.appendChild(row);
		
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('format') + ':');
	
	row.appendChild(td);
	
	var imageFormatSelect = document.createElement('select');
	imageFormatSelect.style.width = '180px';

	var pngOption = document.createElement('option');
	pngOption.setAttribute('value', 'png');
	mxUtils.write(pngOption, mxResources.get('formatPng'));
	imageFormatSelect.appendChild(pngOption);

	var gifOption = document.createElement('option');
	
	if (ExportDialog.showGifOption)
	{
		gifOption.setAttribute('value', 'gif');
		mxUtils.write(gifOption, mxResources.get('formatGif'));
		imageFormatSelect.appendChild(gifOption);
	}
	
	var jpgOption = document.createElement('option');
	jpgOption.setAttribute('value', 'jpg');
	mxUtils.write(jpgOption, mxResources.get('formatJpg'));
	imageFormatSelect.appendChild(jpgOption);
	
	var svgOption = document.createElement('option');
	svgOption.setAttribute('value', 'svg');
	mxUtils.write(svgOption, mxResources.get('formatSvg'));
	imageFormatSelect.appendChild(svgOption);
	
	if (ExportDialog.showXmlOption)
	{
		var xmlOption = document.createElement('option');
		xmlOption.setAttribute('value', 'xml');
		mxUtils.write(xmlOption, mxResources.get('formatXml'));
		imageFormatSelect.appendChild(xmlOption);
	}

	td = document.createElement('td');
	td.appendChild(imageFormatSelect);
	row.appendChild(td);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');

	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('zoom') + ' (%):');
	
	row.appendChild(td);
	
	var zoomInput = document.createElement('input');
	zoomInput.setAttribute('type', 'number');
	zoomInput.setAttribute('value', '100');
	zoomInput.style.width = '180px';

	td = document.createElement('td');
	td.appendChild(zoomInput);
	row.appendChild(td);

	tbody.appendChild(row);

	row = document.createElement('tr');

	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('width') + ':');
	
	row.appendChild(td);
	
	var widthInput = document.createElement('input');
	widthInput.setAttribute('value', width);
	widthInput.style.width = '180px';

	td = document.createElement('td');
	td.appendChild(widthInput);
	row.appendChild(td);

	tbody.appendChild(row);
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('height') + ':');
	
	row.appendChild(td);
	
	var heightInput = document.createElement('input');
	heightInput.setAttribute('value', height);
	heightInput.style.width = '180px';

	td = document.createElement('td');
	td.appendChild(heightInput);
	row.appendChild(td);

	tbody.appendChild(row);
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('dpi') + ':');
	
	row.appendChild(td);
	
	var dpiSelect = document.createElement('select');
	dpiSelect.style.width = '180px';

	var dpi100Option = document.createElement('option');
	dpi100Option.setAttribute('value', '100');
	mxUtils.write(dpi100Option, '100dpi');
	dpiSelect.appendChild(dpi100Option);

	var dpi200Option = document.createElement('option');
	dpi200Option.setAttribute('value', '200');
	mxUtils.write(dpi200Option, '200dpi');
	dpiSelect.appendChild(dpi200Option);
	
	var dpi300Option = document.createElement('option');
	dpi300Option.setAttribute('value', '300');
	mxUtils.write(dpi300Option, '300dpi');
	dpiSelect.appendChild(dpi300Option);
	
	var dpi400Option = document.createElement('option');
	dpi400Option.setAttribute('value', '400');
	mxUtils.write(dpi400Option, '400dpi');
	dpiSelect.appendChild(dpi400Option);
	
	var dpiCustOption = document.createElement('option');
	dpiCustOption.setAttribute('value', 'custom');
	mxUtils.write(dpiCustOption, mxResources.get('custom'));
	dpiSelect.appendChild(dpiCustOption);

	var customDpi = document.createElement('input');
	customDpi.style.width = '180px';
	customDpi.style.display = 'none';
	customDpi.setAttribute('value', '100');
	customDpi.setAttribute('type', 'number');
	customDpi.setAttribute('min', '50');
	customDpi.setAttribute('step', '50');
	
	var zoomUserChanged = false;
	
	mxEvent.addListener(dpiSelect, 'change', function()
	{
		if (this.value == 'custom')
		{
			this.style.display = 'none';
			customDpi.style.display = '';
			customDpi.focus();
		}
		else
		{
			customDpi.value = this.value;
			
			if (!zoomUserChanged) 
			{
				zoomInput.value = this.value;
			}
		}
	});
	
	mxEvent.addListener(customDpi, 'change', function()
	{
		var dpi = parseInt(customDpi.value);
		
		if (isNaN(dpi) || dpi <= 0)
		{
			customDpi.style.backgroundColor = 'red';
		}
		else
		{
			customDpi.style.backgroundColor = '';

			if (!zoomUserChanged) 
			{
				zoomInput.value = dpi;
			}
		}	
	});
	
	td = document.createElement('td');
	td.appendChild(dpiSelect);
	td.appendChild(customDpi);
	row.appendChild(td);

	tbody.appendChild(row);
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('background') + ':');
	
	row.appendChild(td);
	
	var transparentCheckbox = document.createElement('input');
	transparentCheckbox.setAttribute('type', 'checkbox');
	transparentCheckbox.checked = graph.background == null || graph.background == mxConstants.NONE;

	td = document.createElement('td');
	td.appendChild(transparentCheckbox);
	mxUtils.write(td, mxResources.get('transparent'));
	
	row.appendChild(td);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');
	
	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('grid') + ':');
	
	row.appendChild(td);
	
	var gridCheckbox = document.createElement('input');
	gridCheckbox.setAttribute('type', 'checkbox');
	gridCheckbox.checked = false;

	td = document.createElement('td');
	td.appendChild(gridCheckbox);
	
	row.appendChild(td);
	
	tbody.appendChild(row);
	
	row = document.createElement('tr');

	td = document.createElement('td');
	td.style.fontSize = '10pt';
	mxUtils.write(td, mxResources.get('borderWidth') + ':');
	
	row.appendChild(td);
	
	var borderInput = document.createElement('input');
	borderInput.setAttribute('type', 'number');
	borderInput.setAttribute('value', ExportDialog.lastBorderValue);
	borderInput.style.width = '180px';

	td = document.createElement('td');
	td.appendChild(borderInput);
	row.appendChild(td);

	tbody.appendChild(row);
	table.appendChild(tbody);
	
	// Handles changes in the export format
	function formatChanged()
	{
		var name = nameInput.value;
		var dot = name.lastIndexOf('.');
		
		if (dot > 0)
		{
			nameInput.value = name.substring(0, dot + 1) + imageFormatSelect.value;
		}
		else
		{
			nameInput.value = name + '.' + imageFormatSelect.value;
		}
		
		if (imageFormatSelect.value === 'xml')
		{
			zoomInput.setAttribute('disabled', 'true');
			widthInput.setAttribute('disabled', 'true');
			heightInput.setAttribute('disabled', 'true');
			borderInput.setAttribute('disabled', 'true');
		}
		else
		{
			zoomInput.removeAttribute('disabled');
			widthInput.removeAttribute('disabled');
			heightInput.removeAttribute('disabled');
			borderInput.removeAttribute('disabled');
		}
		
		if (imageFormatSelect.value === 'png' || imageFormatSelect.value === 'svg' || imageFormatSelect.value === 'pdf')
		{
			transparentCheckbox.removeAttribute('disabled');
		}
		else
		{
			transparentCheckbox.setAttribute('disabled', 'disabled');
		}
		
		if (imageFormatSelect.value === 'png' || imageFormatSelect.value === 'jpg' || imageFormatSelect.value === 'pdf')
		{
			gridCheckbox.removeAttribute('disabled');
		}
		else
		{
			gridCheckbox.setAttribute('disabled', 'disabled');
		}
		
		if (imageFormatSelect.value === 'png')
		{
			dpiSelect.removeAttribute('disabled');
			customDpi.removeAttribute('disabled');
		}
		else
		{
			dpiSelect.setAttribute('disabled', 'disabled');
			customDpi.setAttribute('disabled', 'disabled');
		}
	};
	
	mxEvent.addListener(imageFormatSelect, 'change', formatChanged);
	formatChanged();

	function checkValues()
	{
		if (widthInput.value * heightInput.value > MAX_AREA || widthInput.value <= 0)
		{
			widthInput.style.backgroundColor = 'red';
		}
		else
		{
			widthInput.style.backgroundColor = '';
		}
		
		if (widthInput.value * heightInput.value > MAX_AREA || heightInput.value <= 0)
		{
			heightInput.style.backgroundColor = 'red';
		}
		else
		{
			heightInput.style.backgroundColor = '';
		}
	};

	mxEvent.addListener(zoomInput, 'change', function()
	{
		zoomUserChanged = true;
		var s = Math.max(0, parseFloat(zoomInput.value) || 100) / 100;
		zoomInput.value = parseFloat((s * 100).toFixed(2));
		
		if (width > 0)
		{
			widthInput.value = Math.floor(width * s);
			heightInput.value = Math.floor(height * s);
		}
		else
		{
			zoomInput.value = '100';
			widthInput.value = width;
			heightInput.value = height;
		}
		
		checkValues();
	});

	mxEvent.addListener(widthInput, 'change', function()
	{
		var s = parseInt(widthInput.value) / width;
		
		if (s > 0)
		{
			zoomInput.value = parseFloat((s * 100).toFixed(2));
			heightInput.value = Math.floor(height * s);
		}
		else
		{
			zoomInput.value = '100';
			widthInput.value = width;
			heightInput.value = height;
		}
		
		checkValues();
	});

	mxEvent.addListener(heightInput, 'change', function()
	{
		var s = parseInt(heightInput.value) / height;
		
		if (s > 0)
		{
			zoomInput.value = parseFloat((s * 100).toFixed(2));
			widthInput.value = Math.floor(width * s);
		}
		else
		{
			zoomInput.value = '100';
			widthInput.value = width;
			heightInput.value = height;
		}
		
		checkValues();
	});
	
	row = document.createElement('tr');
	td = document.createElement('td');
	td.setAttribute('align', 'right');
	td.style.paddingTop = '22px';
	td.colSpan = 2;
	
	var saveBtn = mxUtils.button(mxResources.get('export'), mxUtils.bind(this, function()
	{
		if (parseInt(zoomInput.value) <= 0)
		{
			mxUtils.alert(mxResources.get('drawingEmpty'));
		}
		else
		{
	    	var name = nameInput.value;
			var format = imageFormatSelect.value;
	    	var s = Math.max(0, parseFloat(zoomInput.value) || 100) / 100;
			var b = Math.max(0, parseInt(borderInput.value));
			var bg = graph.background;
			var dpi = Math.max(1, parseInt(customDpi.value));
			
			if ((format == 'svg' || format == 'png' || format == 'pdf') && transparentCheckbox.checked)
			{
				bg = null;
			}
			else if (bg == null || bg == mxConstants.NONE)
			{
				bg = '#ffffff';
			}
			
			ExportDialog.lastBorderValue = b;
			ExportDialog.exportFile(editorUi, name, format, bg, s, b, dpi, gridCheckbox.checked);
		}
	}));
	saveBtn.className = 'geBtn gePrimaryBtn';
	
	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		td.appendChild(cancelBtn);
		td.appendChild(saveBtn);
	}
	else
	{
		td.appendChild(saveBtn);
		td.appendChild(cancelBtn);
	}

	row.appendChild(td);
	tbody.appendChild(row);
	table.appendChild(tbody);
	this.container = table;
};

/**
 * Remembers last value for border.
 */
ExportDialog.lastBorderValue = 0;

/**
 * Global switches for the export dialog.
 */
ExportDialog.showGifOption = true;

/**
 * Global switches for the export dialog.
 */
ExportDialog.showXmlOption = true;

/**
 * Hook for getting the export format. Returns null for the default
 * intermediate XML export format or a function that returns the
 * parameter and value to be used in the request in the form
 * key=value, where value should be URL encoded.
 */
ExportDialog.exportFile = function(editorUi, name, format, bg, s, b, dpi, grid)
{
	var graph = editorUi.editor.graph;
	
	if (format == 'xml')
	{
    	ExportDialog.saveLocalFile(editorUi, mxUtils.getXml(editorUi.editor.getGraphXml()), name, format);
	}
    else if (format == 'svg')
	{
		ExportDialog.saveLocalFile(editorUi, mxUtils.getXml(graph.getSvg(bg, s, b)), name, format);
	}
    else
    {
    	var bounds = graph.getGraphBounds();
    	
		// New image export
		var xmlDoc = mxUtils.createXmlDocument();
		var root = xmlDoc.createElement('output');
		xmlDoc.appendChild(root);
		
	    // Renders graph. Offset will be multiplied with state's scale when painting state.
		var xmlCanvas = new mxXmlCanvas2D(root);
		xmlCanvas.translate(Math.floor((b / s - bounds.x) / graph.view.scale),
			Math.floor((b / s - bounds.y) / graph.view.scale));
		xmlCanvas.scale(s / graph.view.scale);
		
		var imgExport = new mxImageExport()
	    imgExport.drawState(graph.getView().getState(graph.model.root), xmlCanvas);
	    
		// Puts request data together
		var param = 'xml=' + encodeURIComponent(mxUtils.getXml(root));
		var w = Math.ceil(bounds.width * s / graph.view.scale + 2 * b);
		var h = Math.ceil(bounds.height * s / graph.view.scale + 2 * b);
		
		// Requests image if request is valid
		if (param.length <= MAX_REQUEST_SIZE && w * h < MAX_AREA)
		{
			editorUi.hideDialog();
			var req = new mxXmlRequest(EXPORT_URL, 'format=' + format +
				'&filename=' + encodeURIComponent(name) +
				'&bg=' + ((bg != null) ? bg : 'none') +
				'&w=' + w + '&h=' + h + '&' + param +
				'&dpi=' + dpi);
			req.simulate(document, '_blank');
		}
		else
		{
			mxUtils.alert(mxResources.get('drawingTooLarge'));
		}
	}
};

/**
 * Hook for getting the export format. Returns null for the default
 * intermediate XML export format or a function that returns the
 * parameter and value to be used in the request in the form
 * key=value, where value should be URL encoded.
 */
ExportDialog.saveLocalFile = function(editorUi, data, filename, format)
{
	if (data.length < MAX_REQUEST_SIZE)
	{
		editorUi.hideDialog();
		var req = new mxXmlRequest(SAVE_URL, 'xml=' + encodeURIComponent(data) + '&filename=' +
			encodeURIComponent(filename) + '&format=' + format);
		req.simulate(document, '_blank');
	}
	else
	{
		mxUtils.alert(mxResources.get('drawingTooLarge'));
		mxUtils.popup(xml);
	}
};

/**
 * Constructs a new metadata dialog.
 */
var EditDataDialog = function(ui, cell, optionalGraph)
{
	var div = document.createElement('div');
	var graph = optionalGraph || ui.editor.graph;
	
	var value = graph.getModel().getValue(cell);
	
	// Converts the value to an XML node
	if (!mxUtils.isNode(value))
	{
		var doc = mxUtils.createXmlDocument();
		var obj = doc.createElement('object');
		obj.setAttribute('label', value || '');
		value = obj;
	}
	
	var meta = {};
	
	try
	{
		var temp = mxUtils.getValue(ui.editor.graph.getCurrentCellStyle(cell), 'metaData', null);
		
		if (temp != null)
		{
			meta = JSON.parse(temp);
		}
	}
	catch (e)
	{
		// ignore
	}
	
	// Creates the dialog contents
	var form = new mxForm('properties');
	form.table.style.width = '100%';

	var attrs = value.attributes;
	var names = [];
	var texts = [];
	var count = 0;

	var id = (EditDataDialog.getDisplayIdForCell != null) ?
		EditDataDialog.getDisplayIdForCell(ui, cell, optionalGraph) : null;
	
	var addRemoveButton = function(text, name)
	{
		var wrapper = document.createElement('div');
		wrapper.style.position = 'relative';
		wrapper.style.display = 'flex';
		wrapper.style.alignItems = 'center';
		wrapper.style.boxSizing = 'border-box';
		wrapper.style.width = '100%';
		
		var removeAttr = document.createElement('a');
		var img = mxUtils.createImage(Dialog.prototype.closeImage);
		img.style.height = '9px';
		img.style.fontSize = '9px';
		
		removeAttr.className = 'geButton';
		removeAttr.setAttribute('title', mxResources.get('delete'));
		removeAttr.style.marginLeft = '8px';
		removeAttr.style.cursor = 'pointer';
		removeAttr.appendChild(img);
		
		var removeAttrFn = (function(name)
		{
			return function()
			{
				var count = 0;
				
				for (var j = 0; j < names.length; j++)
				{
					if (names[j] == name)
					{
						texts[j] = null;
						form.table.deleteRow(count + ((id != null) ? 1 : 0));
						
						break;
					}
					
					if (texts[j] != null)
					{
						count++;
					}
				}
			};
		})(name);
		
		mxEvent.addListener(removeAttr, 'click', removeAttrFn);
		
		var parent = text.parentNode;
		wrapper.appendChild(text);
		wrapper.appendChild(removeAttr);
		parent.appendChild(wrapper);
	};
	
	var addTextArea = function(index, name, value)
	{
		names[index] = name;
		texts[index] = form.addTextarea(names[count] + ':', value, 2);
		texts[index].style.width = '100%';
		
		if (value.indexOf('\n') > 0)
		{
			texts[index].setAttribute('rows', '2');
		}
		
		addRemoveButton(texts[index], name);
		
		if (meta[name] != null && meta[name].editable == false)
		{
			texts[index].setAttribute('disabled', 'disabled');
		}
	};
	
	var temp = [];
	var isLayer = graph.getModel().getParent(cell) == graph.getModel().getRoot();
	var style = graph.getCellStyle(cell);

	for (var i = 0; i < attrs.length; i++)
	{
		if ((attrs[i].nodeName != 'label' || style['metaEdit'] == '1' ||
			Graph.translateDiagram || isLayer) &&
			attrs[i].nodeName != 'placeholders')
		{
			temp.push({name: attrs[i].nodeName, value: attrs[i].nodeValue});
		}
	}
	
	// Sorts by name
	temp.sort(function(a, b)
	{
		if (a.name == 'label')
		{
			return 1;
		}
		else if (b.name == 'label')
		{
			return -1;
		}
	    else if (a.name < b.name)
	    {
	    	return -1;
	    }
	    else if (a.name > b.name)
	    {
	    	return 1;
	    }
	    else
	    {
	    	return 0;
	    }
	});

	if (id != null)
	{	
		var text = document.createElement('div');
		text.style.width = '100%';
		text.style.fontSize = '11px';
		text.style.textAlign = 'center';
		mxUtils.write(text, id);
		
		var idInput = form.addField(mxResources.get('id') + ':', text);
		
		mxEvent.addListener(text, 'dblclick', function(evt)
		{
			var dlg = new FilenameDialog(ui, id, mxResources.get('apply'),
				mxUtils.bind(this, function(value)
			{
				if (value != null && value.length > 0 && value != id)
				{
					if (graph.model.isRoot(cell))
					{
						var page = ui.getPageById(id);

						if (page != null)
						{
							if (ui.getPageById(value) == null)
							{
								var index = ui.getPageIndex(page);

								if (index >= 0)
								{
									ui.removePage(page);
									page.node.setAttribute('id', value);
									id = value;
									idInput.innerHTML = mxUtils.htmlEntities(value);
									ui.insertPage(page, index);
								}
							}
							else
							{
								ui.handleError({message: mxResources.get('alreadyExst',
									[mxResources.get('page')])});
							}
						}
					}
					else
					{
						if (graph.getModel().getCell(value) == null)
						{
							graph.getModel().cellRemoved(cell);
							cell.setId(value);
							id = value;
							idInput.innerHTML = mxUtils.htmlEntities(value);
							graph.getModel().cellAdded(cell);
						}
						else
						{
							ui.handleError({message: mxResources.get('alreadyExst', [value])});
						}
					}
				}
			}), mxResources.get('id'));
			ui.showDialog(dlg.container, 300, 80, true, true);
			dlg.init();
		});
	}
	
	for (var i = 0; i < temp.length; i++)
	{
		addTextArea(count, temp[i].name, temp[i].value);
		count++;
	}
	
	var top = document.createElement('div');
	top.style.position = 'absolute';
	top.style.top = '30px';
	top.style.left = '30px';
	top.style.right = '30px';
	top.style.bottom = '80px';
	top.style.overflowY = 'auto';
	
	top.appendChild(form.table);

	var newProp = document.createElement('div');
	newProp.style.display = 'flex';
	newProp.style.alignItems = 'center';
	newProp.style.boxSizing = 'border-box';
	newProp.style.paddingRight = '160px';
	newProp.style.whiteSpace = 'nowrap';
	newProp.style.marginTop = '6px';
	newProp.style.width = '100%';
	
	var nameInput = document.createElement('input');
	nameInput.setAttribute('placeholder', mxResources.get('enterPropertyName'));
	nameInput.setAttribute('type', 'text');
	nameInput.setAttribute('size', (mxClient.IS_IE || mxClient.IS_IE11) ? '36' : '40');
	nameInput.style.boxSizing = 'border-box';
	nameInput.style.borderWidth = '1px';
	nameInput.style.borderStyle = 'solid';
	nameInput.style.marginLeft = '2px';
	nameInput.style.padding = '4px';
	nameInput.style.width = '100%';
	
	newProp.appendChild(nameInput);
	top.appendChild(newProp);
	div.appendChild(top);
	
	var addBtn = mxUtils.button(mxResources.get('addProperty'), function()
	{
		var name = nameInput.value;

		// Avoid ':' in attribute names which seems to be valid in Chrome
		if (name.length > 0 && name != 'label' && name != 'id' &&
			name != 'placeholders' && name.indexOf(':') < 0)
		{
			try
			{
				var idx = mxUtils.indexOf(names, name);
				
				if (idx >= 0 && texts[idx] != null)
				{
					texts[idx].focus();
				}
				else
				{
					// Checks if the name is valid
					var clone = value.cloneNode(false);
					clone.setAttribute(name, '');
					
					if (idx >= 0)
					{
						names.splice(idx, 1);
						texts.splice(idx, 1);
					}

					names.push(name);
					var text = form.addTextarea(name + ':', '', 2);
					text.style.width = '100%';
					texts.push(text);
					addRemoveButton(text, name);

					text.focus();
				}

				addBtn.setAttribute('disabled', 'disabled');
				nameInput.value = '';
			}
			catch (e)
			{
				mxUtils.alert(e);
			}
		}
		else
		{
			mxUtils.alert(mxResources.get('invalidName'));
		}
	});

	mxEvent.addListener(nameInput, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			addBtn.click();
		}
	});
	
	this.init = function()
	{
		if (texts.length > 0)
		{
			texts[0].focus();
		}
		else
		{
			nameInput.focus();
		}
	};
	
	addBtn.setAttribute('title', mxResources.get('addProperty'));
	addBtn.setAttribute('disabled', 'disabled');
	addBtn.style.textOverflow = 'ellipsis';
	addBtn.style.position = 'absolute';
	addBtn.style.overflow = 'hidden';
	addBtn.style.width = '144px';
	addBtn.style.right = '0px';
	addBtn.className = 'geBtn';
	newProp.appendChild(addBtn);

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		ui.hideDialog.apply(ui, arguments);
	});
	
	cancelBtn.setAttribute('title', 'Escape');
	cancelBtn.className = 'geBtn';

	var exportBtn = mxUtils.button(mxResources.get('export'), mxUtils.bind(this, function(evt)
	{
		var result = graph.getDataForCells([cell], true);

		var dlg = new EmbedDialog(ui, JSON.stringify(result, null, 2), null, null, function()
		{
			console.log(result);
			ui.alert('Written to Console (Dev Tools)');
		}, mxResources.get('export'), null, 'Console', 'data.json');
		ui.showDialog(dlg.container, 450, 270, true, true, null,
			false, null, new mxRectangle(0, 0, 400, 250));
		dlg.init();
	}));
	
	exportBtn.setAttribute('title', mxResources.get('export'));
	exportBtn.className = 'geBtn';
	
	var applyBtn = mxUtils.button(mxResources.get('apply'), function()
	{
		try
		{
			ui.hideDialog.apply(ui, arguments);
			
			// Clones and updates the value
			value = value.cloneNode(true);
			var removeLabel = false;
			
			for (var i = 0; i < names.length; i++)
			{
				if (texts[i] == null)
				{
					value.removeAttribute(names[i]);
				}
				else
				{
					value.setAttribute(names[i], texts[i].value);
					removeLabel = removeLabel || (names[i] == 'placeholder' &&
						value.getAttribute('placeholders') == '1');
				}
			}
			
			// Removes label if placeholder is assigned
			if (removeLabel)
			{
				value.removeAttribute('label');
			}
			
			// Updates the value of the cell (undoable)
			graph.getModel().setValue(cell, value);
		}
		catch (e)
		{
			mxUtils.alert(e);
		}
	});

	applyBtn.setAttribute('title', 'Ctrl+Enter');
	applyBtn.className = 'geBtn gePrimaryBtn';

	mxEvent.addListener(div, 'keydown', function(e)
	{
		if (e.keyCode == 13 && mxEvent.isControlDown(e))
		{
			applyBtn.click();
		}
	});
	
	function updateAddBtn()
	{
		if (nameInput.value.length > 0)
		{
			addBtn.removeAttribute('disabled');
		}
		else
		{
			addBtn.setAttribute('disabled', 'disabled');
		}
	};

	mxEvent.addListener(nameInput, 'keyup', updateAddBtn);
	
	// Catches all changes that don't fire a keyup (such as paste via mouse)
	mxEvent.addListener(nameInput, 'change', updateAddBtn);
	
	var buttons = document.createElement('div');
	buttons.style.display = 'flex';
	buttons.style.justifyContent = 'flex-end';
	buttons.style.alignItems = 'center';
	buttons.style.position = 'absolute';
	buttons.style.left = '30px';
	buttons.style.right = '30px';
	buttons.style.bottom = '30px';
	buttons.style.height = '40px';
	
	if (ui.editor.graph.getModel().isVertex(cell) || ui.editor.graph.getModel().isEdge(cell))
	{
		var replace = document.createElement('span');
		replace.style.marginRight = '10px';
		replace.style.justifyContent = 'flex-end';
		replace.style.alignItems = 'center';
		replace.style.display = 'flex';
		replace.style.whiteSpace = 'nowrap';

		var input = document.createElement('input');
		input.setAttribute('type', 'checkbox');
		input.style.marginRight = '6px';
		
		if (value.getAttribute('placeholders') == '1')
		{
			input.setAttribute('checked', 'checked');
			input.defaultChecked = true;
		}
	
		mxEvent.addListener(input, 'click', function()
		{
			if (value.getAttribute('placeholders') == '1')
			{
				value.removeAttribute('placeholders');
			}
			else
			{
				value.setAttribute('placeholders', '1');
			}
		});
		
		replace.appendChild(input);
		mxUtils.write(replace, mxResources.get('placeholders'));
		
		if (EditDataDialog.placeholderHelpLink != null)
		{
			replace.appendChild(ui.createHelpIcon(
				EditDataDialog.placeholderHelpLink));
		}
		
		buttons.appendChild(replace);
	}
	
	if (ui.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}
	
	buttons.appendChild(exportBtn);
	buttons.appendChild(applyBtn);

	if (!ui.editor.cancelFirst)
	{
		buttons.appendChild(cancelBtn);
	}

	div.appendChild(buttons);
	this.container = div;
};

/**
 * Optional help link.
 */
EditDataDialog.getDisplayIdForCell = function(ui, cell)
{
	var id = null;
	
	if (ui.editor.graph.getModel().getParent(cell) != null)
	{
		id = cell.getId();
	}
	
	return id;
};

/**
 * Optional help link.
 */
EditDataDialog.placeholderHelpLink = null;

/**
 * Constructs a new link dialog.
 */
var LinkDialog = function(editorUi, initialValue, btnLabel, fn)
{
	var div = document.createElement('div');
	mxUtils.write(div, mxResources.get('editLink') + ':');
	
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
	linkInput.setAttribute('placeholder', 'http://www.example.com/');
	linkInput.setAttribute('type', 'text');
	linkInput.style.marginTop = '6px';
	linkInput.style.width = '400px';
	linkInput.style.backgroundImage = 'url(\'' + Dialog.prototype.clearImage + '\')';
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
	cross.style.background = 'url(' + IMAGE_PATH + '/transparent.gif)';

	mxEvent.addListener(cross, 'click', function()
	{
		linkInput.value = '';
		linkInput.focus();
	});
	
	inner.appendChild(linkInput);
	inner.appendChild(cross);
	div.appendChild(inner);
	
	this.init = function()
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
	};
	
	var btns = document.createElement('div');
	btns.style.marginTop = '18px';
	btns.style.textAlign = 'right';

	mxEvent.addListener(linkInput, 'keypress', function(e)
	{
		if (e.keyCode == 13)
		{
			editorUi.hideDialog();
			fn(linkInput.value);
		}
	});

	var cancelBtn = mxUtils.button(mxResources.get('cancel'), function()
	{
		editorUi.hideDialog();
	});
	cancelBtn.className = 'geBtn';
	
	if (editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}
	
	var mainBtn = mxUtils.button(btnLabel, function()
	{
		editorUi.hideDialog();
		fn(linkInput.value);
	});
	mainBtn.className = 'geBtn gePrimaryBtn';
	btns.appendChild(mainBtn);
	
	if (!editorUi.editor.cancelFirst)
	{
		btns.appendChild(cancelBtn);
	}

	div.appendChild(btns);

	this.container = div;
};

/**
 * 
 */
var OutlineWindow = function(editorUi, x, y, w, h)
{
	var graph = editorUi.editor.graph;

	var div = document.createElement('div');
	div.style.position = 'absolute';
	div.style.width = '100%';
	div.style.height = '100%';
	div.style.overflow = 'hidden';

	this.window = new mxWindow(mxResources.get('outline'), div, x, y, w, h, true, true);
	this.window.minimumSize = new mxRectangle(0, 0, 80, 80);
	this.window.destroyOnClose = false;
	this.window.setMaximizable(false);
	this.window.setResizable(true);
	this.window.setClosable(true);
	this.window.setVisible(true);
	
	var outline = editorUi.createOutline(this.window);

	editorUi.installResizeHandler(this, true, function()
	{
		outline.destroy();
	});

	this.window.addListener(mxEvent.SHOW, mxUtils.bind(this, function()
	{
		this.window.fit();
		outline.setSuspended(false);
	}));
	
	this.window.addListener(mxEvent.HIDE, mxUtils.bind(this, function()
	{
		outline.setSuspended(true);
	}));
	
	this.window.addListener(mxEvent.NORMALIZE, mxUtils.bind(this, function()
	{
		outline.setSuspended(false);
	}));
			
	this.window.addListener(mxEvent.MINIMIZE, mxUtils.bind(this, function()
	{
		outline.setSuspended(true);
	}));

	outline.init(div);
	
	mxEvent.addMouseWheelListener(function(evt, up)
	{
		var outlineWheel = false;
		var source = mxEvent.getSource(evt);

		while (source != null)
		{
			if (source == outline.svg)
			{
				outlineWheel = true;
				break;
			}

			source = source.parentNode;
		}

		if (outlineWheel)
		{
			var factor = graph.zoomFactor;

			// Slower zoom for pinch gesture on trackpad
			if (evt.deltaY != null && Math.round(evt.deltaY) != evt.deltaY)
			{
				factor = 1 + (Math.abs(evt.deltaY) / 20) * (factor - 1);
			}

			graph.lazyZoom(up, null, null, factor);
			mxEvent.consume(evt);
		}
	});
};

/**
 * 
 */
var LayersWindow = function(editorUi, x, y, w, h)
{
	var graph = editorUi.editor.graph;
	var model = graph.getModel();
	
	var div = document.createElement('div');
	div.style.userSelect = 'none';
	div.style.height = '100%';
	div.style.marginBottom = '10px';
	div.style.overflow = 'auto';

	var listDiv = document.createElement('div')
	listDiv.style.position = 'absolute';
	listDiv.style.overflow = 'auto';
	listDiv.style.left = '0px';
	listDiv.style.right = '0px';
	listDiv.style.top = '0px';
	listDiv.style.bottom = '32px';
	div.appendChild(listDiv);
	
	var dragSource = null;
	var dropIndex = null;
	
	mxEvent.addListener(div, 'dragover', function(evt)
	{
		evt.dataTransfer.dropEffect = 'move';
		dropIndex = 0;
		evt.stopPropagation();
		evt.preventDefault();
	});
	
	// Workaround for "no element found" error in FF
	mxEvent.addListener(div, 'drop', function(evt)
	{
		evt.stopPropagation();
		evt.preventDefault();
	});

	var layerCount = null;
	var selectionLayer = null;
	var layerDivs = new mxDictionary();
	var ldiv = document.createElement('div');
	ldiv.className = 'geToolbarContainer geDialogToolbar';
	
	var link = document.createElement('a');
	link.className = 'geButton';
	
	var addLink = link.cloneNode(false);
	addLink.style.backgroundImage = 'url(' + Editor.plusImage + ')';
	addLink.setAttribute('title', mxResources.get('addLayer'));

	mxEvent.addListener(addLink, 'click', function(evt)
	{
		if (graph.isEnabled())
		{
			selectLayers(false);
			model.beginUpdate();
			var cell
			
			try
			{
				cell = graph.addCell(new mxCell(mxResources.get('untitledLayer')), model.root);
				graph.setDefaultParent(cell);
			}
			finally
			{
				model.endUpdate();
			}

			renameLayer(cell);
		}
		
		mxEvent.consume(evt);
	});
	
	if (!graph.isEnabled())
	{
		addLink.classList.add('mxDisabled');
	}
	
	ldiv.appendChild(addLink);
	
	function renameLayer(layer)
	{
		if (graph.isEnabled() && layer != null)
		{
			var div = layerDivs.get(layer);

			if (div != null)
			{
				var spans = div.getElementsByTagName('div');

				if (spans != null && spans.length > 1)
				{
					var span = spans[1];
					var oldValue = mxUtils.getTextContent(span);
					span.style.textOverflow = '';
					span.style.cursor = 'text';
					span.contentEditable = 'true';
					span.focus();
					document.execCommand('selectAll', false, null);

					// Stops drag and drop on parent div
					div.removeAttribute('draggable');
					div.style.cursor = '';

					var stopEditing = function(applyValue)
					{
						if (span.contentEditable == 'true')
						{
							span.contentEditable = 'false';
							var newValue = mxUtils.getTextContent(span);

							if (applyValue && newValue != oldValue)
							{
								var newValue = mxUtils.getTextContent(span);

								if (newValue.length > 0)
								{
									graph.cellLabelChanged(layer, newValue);
								}
								else
								{
									graph.cellLabelChanged(layer, mxResources.get('untitledLayer'));
								}
							}
							else
							{
								refresh();
							}
						}
					};

					mxEvent.addListener(span, 'keydown', function(evt)
					{
						if (evt.keyCode == 13 || evt.keyCode == 27)
						{
							stopEditing(evt.keyCode == 13);
							mxEvent.consume(evt);
						}
					});

					mxEvent.addListener(span, 'blur', function(evt)
					{
						stopEditing(true);
						mxEvent.consume(evt);
					});
				}
			}
		}
	};
	
	var menuLink = link.cloneNode(false);
	menuLink.style.backgroundImage = 'url(' + Editor.menuImage + ')';
	ldiv.appendChild(menuLink);

	function isLayersVisible(layers)
	{
		var visible = true;

		for (var i = 0; i < layers.length; i++)
		{
			if (!model.isVisible(layers[i]))
			{
				visible = false;
				break;
			}
		}

		return visible;
	};

	function selectLayers(selected)
	{
		var divs = layerDivs.getValues();
		
		for (var i = 0; i < divs.length; i++)
		{
			var cb = divs[i].getElementsByTagName('input')[0];
			
			if (cb != null)
			{
				cb.checked = (selected != null) ?
					selected : !cb.checked;
			}
		}
	};

	function isLayerSelected(layer)
	{
		var div = layerDivs.get(layer);
		
		if (div != null)
		{
			var cb = div.getElementsByTagName('input')[0];
			return cb != null && cb.checked;
		}
		
		return false;
	};

	function getSelectedLayers(exclude, ignoreCheckbox)
	{
		var layers = [];

		for (var i = 0; i < model.getChildCount(model.root); i++)
		{
			var layer = model.getChildAt(model.root, i);

			if (layer != exclude && (ignoreCheckbox ||
				isLayerSelected(layer)))
			{
				layers.push(layer);
			}
		}

		return layers;
	};

	function setLayersLocked(layers, locked)
	{
		graph.setCellStyles('locked', (locked) ? '1' : '0', layers);

		if (locked)
		{
			for (var i = 0; i < layers.length; i++)
			{
				graph.removeSelectionCells(model.getDescendants(layers[i]));
			}
		}
	};

	mxEvent.addListener(menuLink, 'click', function(evt)
	{
		if (graph.isEnabled())
		{
			editorUi.editor.graph.popupMenuHandler.hideMenu();
			
			var selectedLayers = getSelectedLayers();
			var index = model.root.getIndex(selectionLayer);
			var enabled = selectedLayers.length > 0 && graph.isEnabled();
			var nothingIsSelected = mxResources.get('nothingIsSelected');
			var layer = graph.getLayerForCells(graph.getSelectionCells());
			var label = graph.convertValueToString(selectionLayer) || mxResources.get('background');

			var menu = new mxPopupMenu(mxUtils.bind(this, function(menu, parent)
			{
				// Adds submenu for multiple selected layers
				var layersSubmenu = menu.addItem(mxResources.get('layers'), null, null, parent);

				menu.addItem(mxResources.get('selectAll'), null, mxUtils.bind(this, function()
				{
					refresh();
					selectLayers(true);
				}), layersSubmenu);

				menu.addItem(mxResources.get('selectNone'), null, mxUtils.bind(this, function()
				{
					refresh();
					selectLayers(false);
				}), layersSubmenu, null, selectedLayers.length > 0).setAttribute('title',
					selectedLayers.length > 0 ? mxResources.get('selectNone') : nothingIsSelected);

				menu.addItem(mxResources.get('invertSelection'), null, mxUtils.bind(this, function()
				{
					refresh();
					selectLayers();
				}), layersSubmenu);

				menu.addSeparator(layersSubmenu);
				
				menu.addItem(mxResources.get('show'), null, mxUtils.bind(this, function()
				{
					graph.setCellsVisible(selectedLayers, true);
				}), layersSubmenu, null, enabled).setAttribute('title', selectedLayers.length > 0 ?
						mxResources.get('show') : nothingIsSelected);
				menu.addItem(mxResources.get('hide'), null, mxUtils.bind(this, function()
				{
					graph.setCellsVisible(selectedLayers, false);
				}), layersSubmenu, null, enabled).setAttribute('title', selectedLayers.length > 0 ?
						mxResources.get('hide') : nothingIsSelected);

				menu.addSeparator(layersSubmenu);

				menu.addItem(mxResources.get('lock'), null, mxUtils.bind(this, function()
				{
					setLayersLocked(getSelectedLayers(), true);
				}), layersSubmenu, null, enabled).setAttribute('title', selectedLayers.length > 0 ?
						mxResources.get('lock') : nothingIsSelected);

				menu.addItem(mxResources.get('unlock'), null, mxUtils.bind(this, function()
				{
					setLayersLocked(getSelectedLayers(), false);
				}), layersSubmenu, null, enabled).setAttribute('title', selectedLayers.length > 0 ?
						mxResources.get('unlock') : nothingIsSelected);
				
				// Adds submenu for single layer operations
				var layerSubmenu = menu.addItem(mxResources.get('currentLayer'), null, null, parent);
				
				menu.addItem(mxResources.get('duplicate'), null, mxUtils.bind(this, function()
				{
					selectLayers(false);
					var newCell = null;
					model.beginUpdate();
					try
					{
						newCell = graph.cloneCell(selectionLayer);
						graph.cellLabelChanged(newCell, mxResources.get('copyOf', [label]));
						newCell = graph.addCell(newCell, model.root, index + 1);
						newCell.setVisible(true);
					}
					finally
					{
						model.endUpdate();
					}

					if (newCell != null && !graph.isCellLocked(newCell))
					{
						graph.setDefaultParent(newCell);
						graph.selectAll(newCell);
					}
				}), layerSubmenu);

				menu.addItem(mxResources.get('addLayer'), null, mxUtils.bind(this, function(evt)
				{
					if (graph.isEnabled())
					{
						selectLayers(false);
						model.beginUpdate();
						var cell
						
						try
						{
							cell = graph.addCell(new mxCell(mxResources.get('untitledLayer')),
								model.root, index + ((mxEvent.isShiftDown(evt) ? 0 : 1)));
							graph.setDefaultParent(cell);
						}
						finally
						{
							model.endUpdate();
						}

						renameLayer(cell);
					}
					
					mxEvent.consume(evt);
				}), layerSubmenu);
				
				menu.addSeparator(layerSubmenu);

				menu.addItem(mxResources.get('rename'), null, mxUtils.bind(this, function()
				{
					renameLayer(selectionLayer);
				}), layerSubmenu);

				menu.addItem(mxResources.get('editData'), null, mxUtils.bind(this, function()
				{
					editorUi.showDataDialog(selectionLayer);
				}), layerSubmenu);

				if (layerCount > 1)
				{
					menu.addSeparator(layerSubmenu);

					menu.addItem(mxResources.get('toFront'), null, mxUtils.bind(this, function()
					{
						graph.addCell(selectionLayer, model.root, layerCount - 1);
					}), layerSubmenu, null, index >= 0 && index < layerCount - 1);

					menu.addItem(mxResources.get('toBack'), null, mxUtils.bind(this, function()
					{
						graph.addCell(selectionLayer, model.root, 0);
					}), layerSubmenu, null, index > 0);

					if (layerCount > 2)
					{
						menu.addItem(mxResources.get('bringForward'), null, mxUtils.bind(this, function()
						{
							graph.addCell(selectionLayer, model.root, index + 1);
						}), layerSubmenu, null, index >= 0 && index < layerCount - 1);

						menu.addItem(mxResources.get('sendBackward'), null, mxUtils.bind(this, function()
						{
							graph.addCell(selectionLayer, model.root, index - 1);
						}), layerSubmenu, null, index > 0);
					}
				}

				menu.addSeparator(layerSubmenu);

				menu.addItem(mxResources.get('selectObjectsInLayer'), null, mxUtils.bind(this, function()
				{
					graph.clearSelection();
					graph.selectAll(selectionLayer);
				}), layerSubmenu, null, selectionLayer.children != null &&
					selectionLayer.children.length > 0);

				// Adds submenu for moving selection cells
				var moveSubmenu = menu.addItem(mxResources.get('moveSelectionTo', ['']),
					null, null, parent, null, !graph.isSelectionEmpty());
				
				if (graph.isSelectionEmpty())
				{
					moveSubmenu.setAttribute('title', mxResources.get('nothingIsSelected'));
				}

				for (var i = layerCount - 1; i >= 0; i--)
				{
					(mxUtils.bind(this, function(child)
					{
						var locked = mxUtils.getValue(graph.getCurrentCellStyle(child), 'locked', '0') == '1';

						var item = menu.addItem(graph.convertValueToString(child) ||
							mxResources.get('background'), null, mxUtils.bind(this, function()
						{
							if (!locked)
							{
								graph.moveCells(graph.getSelectionCells(), 0, 0, false, child);
							}
						}), moveSubmenu, null, !locked);

						if (locked)
						{
							item.setAttribute('title', mxResources.get('locked'));
						}

						if (child == layer)
						{
							menu.addCheckmark(item, Editor.checkmarkImage);
						}
					}))(model.getChildAt(model.root, i));
				}
			}));
			
			menu.smartSeparators = true;
			menu.showDisabled = true;
			menu.autoExpand = true;
			
			// Disables autoexpand and destroys menu when hidden
			menu.hideMenu = mxUtils.bind(this, function()
			{
				mxPopupMenu.prototype.hideMenu.apply(menu, arguments);
				menu.destroy();
			});
		
			var x = mxEvent.getClientX(evt);
			var y = mxEvent.getClientY(evt);
			menu.popup(x, y, null, evt);
			
			// Allows hiding by clicking on document
			editorUi.setCurrentMenu(menu);
			mxEvent.consume(evt);
		}
	});
	
	if (!graph.isEnabled())
	{
		menuLink.classList.add('mxDisabled');
	}
	
	var removeLink = link.cloneNode(false);
	removeLink.style.backgroundImage = 'url(' + Editor.trashImage + ')';
	removeLink.setAttribute('title', mxResources.get('delete'));
	ldiv.appendChild(removeLink);

	mxEvent.addListener(removeLink, 'click', function(evt)
	{
		if (graph.isEnabled())
		{
			model.beginUpdate();
			try
			{
				var index = model.root.getIndex(selectionLayer);
				var layers = getSelectedLayers();

				if (layers.length == 0)
				{
					layers.push(selectionLayer);
				}
				
				graph.removeCells(layers, false);
				
				// Creates default layer if no layer exists
				if (model.getChildCount(model.root) == 0)
				{
					model.add(model.root, new mxCell());
					graph.setDefaultParent(null);
				}
				else if (index > 0 && index <= model.getChildCount(model.root))
				{
					graph.setDefaultParent(model.getChildAt(model.root, index - 1));
				}
				else
				{
					graph.setDefaultParent(null);
				}
			}
			finally
			{
				model.endUpdate();
			}
		}
		
		mxEvent.consume(evt);
	});
	
	if (!graph.isEnabled())
	{
		removeLink.classList.add('mxDisabled');
	}

	div.appendChild(ldiv);
	
	var dot = document.createElement('span');
	dot.setAttribute('title', mxResources.get('allSelectedObjectsInThisLayer'));
	dot.innerHTML = '&#8226;';
	dot.style.padding = '0 2px';
	dot.style.fontSize = '16pt';
	dot.style.order = '1';
	
	function updateLayerDot()
	{
		var div = layerDivs.get(graph.getLayerForCells(graph.getSelectionCells()));
		
		if (div != null)
		{
			div.appendChild(dot);
		}
		else if (dot.parentNode != null)
		{
			dot.parentNode.removeChild(dot);
		}
	};

	function refresh()
	{
		if (graph.isEnabled())
		{
			removeLink.classList.remove('mxDisabled');
			menuLink.classList.remove('mxDisabled');
			addLink.classList.remove('mxDisabled');
		}
		else
		{
			removeLink.classList.add('mxDisabled');
			menuLink.classList.add('mxDisabled');
			addLink.classList.add('mxDisabled');
		}
		
		layerCount = model.getChildCount(model.root)
		listDiv.innerText = '';
		var newLayerDivs = new mxDictionary();
		
		function addLayer(index, label, child, defaultParent, selected)
		{
			var ldiv = document.createElement('div');
			ldiv.className = 'geToolbarContainer';
			ldiv.style.overflow = 'hidden';
			ldiv.style.position = 'relative';
			ldiv.style.height = '30px';
			ldiv.style.display = 'flex';
			ldiv.style.padding = '0 8px';
			ldiv.style.alignItems = 'center';
			ldiv.style.justifyContent = 'flex-start';
			ldiv.style.borderWidth = '0px 0px 1px 0px';
			ldiv.style.borderStyle = 'solid';
			ldiv.style.whiteSpace = 'nowrap';
			ldiv.style.cursor = 'move';
			ldiv.setAttribute('draggable', 'true');
			ldiv.setAttribute('title', label  +
				' (' + child.getId() + ')');
			newLayerDivs.put(child, ldiv);

			var cb = document.createElement('input');
			cb.setAttribute('type', 'checkbox');
			cb.style.cursor = 'pointer';
			cb.style.order = '2';
			cb.checked = selected;
			cb.style.display = (graph.isEnabled()) ? '' : 'none';
			ldiv.appendChild(cb);
			
			var div = document.createElement('div');
			div.style.display = 'flex';
			div.style.alignItems = 'center';
			div.style.minWidth = '0';
			div.style.flexGrow = '1';

			mxEvent.addListener(cb, 'click', function(evt)
			{
				if (mxEvent.isShiftDown(evt))
				{
					selectLayers(cb.checked);
				}
			});

			var title = document.createElement('div');
			mxUtils.write(title, label);
			title.style.whiteSpace = 'nowrap';
			title.style.overflow = 'hidden';
			title.style.textOverflow = 'ellipsis';
			title.style.marginRight = '4px';
			title.style.padding = '4px';
			title.style.flexGrow = '1';

			mxEvent.addListener(ldiv, 'dragover', function(evt)
			{
				evt.dataTransfer.dropEffect = 'move';
				dropIndex = index;
				evt.stopPropagation();
				evt.preventDefault();
			});
			
			mxEvent.addListener(ldiv, 'dragstart', function(evt)
			{
				if (title.contentEditable != 'true')
				{
					dragSource = ldiv;
					
					// Workaround for no DnD on DIV in FF
					if (mxClient.IS_FF)
					{
						// LATER: Check what triggers a parse as XML on this in FF after drop
						evt.dataTransfer.setData('Text', '<layer/>');
					}
				}
			});
			
			mxEvent.addListener(ldiv, 'dragend', function(evt)
			{
				var layers = getSelectedLayers();

				if (dragSource != null && dropIndex != null &&
					model.getChildCount(model.root) > 1)
				{
					layers = (layers.length == 0) ? [child] : layers;

					// Moves all selected layers
					model.beginUpdate();
					try
					{
						for (var i = 0; i < layers.length; i++)
						{
							graph.addCell(layers[i], model.root, dropIndex);
						}
					}
					finally
					{
						model.endUpdate();
					}
				}

				dragSource = null;
				dropIndex = null;
				evt.stopPropagation();
				evt.preventDefault();
			});

			var visible = model.isVisible(child);
			var inp = document.createElement('img');
			inp.className = 'geAdaptiveAsset';
			inp.style.width = '16px';
			inp.style.padding = '0px 6px 0 0';
			inp.style.cursor = 'pointer';
			inp.setAttribute('title', mxResources.get(
				visible ? 'hide' : 'show'));

			if (visible)
			{
				inp.setAttribute('src', Editor.visibleImage);
				mxUtils.setOpacity(div, 90);
			}
			else
			{
				inp.setAttribute('src', Editor.hiddenImage);
				mxUtils.setOpacity(div, 40);
			}

			if (!graph.isEnabled())
			{
				mxUtils.setOpacity(inp, 50);
			}

			div.appendChild(inp);

			mxEvent.addListener(inp, 'click', function(evt)
			{
				if (graph.isEnabled())
				{
					if (mxEvent.isShiftDown(evt))
					{
						var others = getSelectedLayers(child, true);
						graph.setCellsVisible(others, !isLayersVisible(others));
					}
					else
					{
						graph.setCellsVisible([child], !visible);
					}
				}

				mxEvent.consume(evt);
			});

			var btn = inp.cloneNode(false);
			var style = graph.getCurrentCellStyle(child);
			btn.setAttribute('title', mxResources.get('lockUnlock'));
			var locked = mxUtils.getValue(style, 'locked', '0') == '1';

			if (locked)
			{
				btn.setAttribute('src', Editor.lockedImage);
				mxUtils.setOpacity(btn, 90);
				ldiv.style.color = 'red';
			}
			else
			{
				btn.setAttribute('src', Editor.unlockedImage);
				mxUtils.setOpacity(btn, 40);
			}
			
			if (graph.isEnabled())
			{
				btn.style.cursor = 'pointer';
			}

			function toggleLock(allLayers)
			{
				var value = null;
				
				model.beginUpdate();
				try
				{
					value = (locked) ? null : '1';

					if (allLayers)
					{
						var parent = model.getParent(child);

						for (var i = 0; i < model.getChildCount(parent); i++)
						{
							graph.setCellStyles('locked', value,
								[model.getChildAt(parent, i)]);
						}
					}
					else
					{
						graph.setCellStyles('locked', value, [child]);
					}
				}
				finally
				{
					model.endUpdate();
				}

				if (value == '1')
				{
					graph.removeSelectionCells(model.getDescendants(child));
				}
			};
			
			mxEvent.addListener(btn, 'click', function(evt)
			{
				if (graph.isEnabled())
				{
					setLayersLocked(mxEvent.isShiftDown(evt) ?
						getSelectedLayers(null, true) : [child],
						!locked);
					mxEvent.consume(evt);
				}
			});

			div.appendChild(btn);
			div.appendChild(title);
			ldiv.appendChild(div);
			
			mxEvent.addListener(ldiv, 'dblclick', function(evt)
			{
				var nodeName = mxEvent.getSource(evt).nodeName;
				
				if (nodeName != 'INPUT' && nodeName != 'IMG' &&
					title.contentEditable != 'true')
				{
					renameLayer(child);
					mxEvent.consume(evt);
				}
			});

			if (graph.getDefaultParent() == child)
			{
				ldiv.classList.add('geActivePage');
				ldiv.style.fontWeight = (graph.isEnabled()) ? 'bold' : '';
				selectionLayer = child;
			}

			mxEvent.addListener(ldiv, 'click', function(evt)
			{
				if (graph.isEnabled() && title.contentEditable != 'true' &&
					mxEvent.getSource(evt) != cb)
				{
					graph.setDefaultParent(defaultParent);
					graph.view.setCurrentRoot(null);

					if (mxEvent.isShiftDown(evt))
					{
						graph.clearSelection();
						graph.selectAll(selectionLayer);
					}

					mxEvent.consume(evt);
				}
			});
			
			listDiv.appendChild(ldiv);
		};
		
		for (var i = layerCount - 1; i >= 0; i--)
		{
			(mxUtils.bind(this, function(child)
			{
				addLayer(i, graph.convertValueToString(child) ||
					mxResources.get('background'), child, child,
					isLayerSelected(child));
			}))(model.getChildAt(model.root, i));
		}

		layerDivs = newLayerDivs;
		updateLayerDot();
	};

	refresh();
	model.addListener(mxEvent.CHANGE, refresh);
	graph.addListener('defaultParentChanged', refresh);
	editorUi.addListener('lockedChanged', refresh);

	graph.selectionModel.addListener(mxEvent.CHANGE, function()
	{
		updateLayerDot();
	});

	this.window = new mxWindow(mxResources.get('layers'), div, x, y, w, h, true, true);
	this.window.minimumSize = new mxRectangle(0, 0, 170, 120);
	this.window.destroyOnClose = false;
	this.window.setMaximizable(false);
	this.window.setResizable(true);
	this.window.setClosable(true);
	this.window.setVisible(true);
	
	this.init = function()
	{
		listDiv.scrollTop = listDiv.scrollHeight - listDiv.clientHeight;	
	};

	this.window.addListener(mxEvent.SHOW, mxUtils.bind(this, function()
	{
		this.window.fit();
	}));
	
	// Make refresh available via instance
	this.refreshLayers = refresh;
	editorUi.installResizeHandler(this, true);
};
